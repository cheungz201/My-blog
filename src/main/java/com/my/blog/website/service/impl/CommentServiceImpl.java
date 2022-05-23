package com.my.blog.website.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.my.blog.website.cache.StringCache;
import com.my.blog.website.exception.TipException;
import com.my.blog.website.utils.DateKit;
import com.my.blog.website.utils.TaleUtils;
import com.my.blog.website.dao.CommentVoMapper;
import com.my.blog.website.model.Bo.CommentBo;
import com.my.blog.website.model.Vo.CommentVo;
import com.my.blog.website.model.Vo.CommentVoExample;
import com.my.blog.website.model.Vo.ContentVo;
import com.my.blog.website.service.ICommentService;
import com.my.blog.website.service.IContentService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import xyz.cheungz.httphelper.utils.SerializationUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by BlueT on 2021/3/16.
 */
@Service
public class CommentServiceImpl implements ICommentService {
    private static final String COMMENTS = "comments";
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Resource
    private CommentVoMapper commentDao;

    @Resource
    private IContentService contentService;

    @Resource
    private StringCache redisStringCache;

    @Override
    public void insertComment(CommentVo comments) {
        if (null == comments) {
            throw new TipException("评论对象为空");
        }
        if (StringUtils.isBlank(comments.getAuthor())) {
            comments.setAuthor("热心网友");
        }
        if (StringUtils.isNotBlank(comments.getMail()) && !TaleUtils.isEmail(comments.getMail())) {
            throw new TipException("请输入正确的邮箱格式");
        }
        if (StringUtils.isBlank(comments.getContent())) {
            throw new TipException("评论内容不能为空");
        }
        if (comments.getContent().length() < 5 || comments.getContent().length() > 2000) {
            throw new TipException("评论字数在5-2000个字符");
        }
        if (null == comments.getCid()) {
            throw new TipException("评论文章不能为空");
        }
        ContentVo contents = contentService.getContents(String.valueOf(comments.getCid()));
        if (null == contents) {
            throw new TipException("不存在的文章");
        }
        comments.setOwnerId(contents.getAuthorId());
        comments.setCreated(DateKit.getCurrentUnixTime());
        commentDao.insertSelective(comments);
        redisStringCache.deleteCache(comments.getCid()+COMMENTS);
        ContentVo temp = new ContentVo();
        temp.setCid(contents.getCid());
        temp.setCommentsNum(contents.getCommentsNum() + 1);
        contentService.updateContentByCid(temp);
    }

    @Override
    public PageInfo<CommentBo> getComments(Integer cid, int page, int limit) {

        PageInfo<CommentBo> returnBo = null;

        try {
            if (null != cid) {
                String cids = cid + COMMENTS;
                String cache = redisStringCache.getCache(cids);
                //缓存不为空
                if (StringUtils.isNotBlank(cache)) {
                    returnBo = SerializationUtil.string2Obj(cache, PageInfo.class);
                    return returnBo;

                } else {   //缓存为空从数据库读取，并且将数据缓存至redis
                    returnBo = queryDataToCache(cid, page, limit);
                    if (returnBo.getSize() != 0) {
                        String s = SerializationUtil.obj2String(returnBo);
                        redisStringCache.addCacheByTime(cids, s, 7, TimeUnit.DAYS);
                        return returnBo;
                    }
                }
            }
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }catch (RedisConnectionFailureException e){
            LOGGER.error(e.getMessage());
            try {
                returnBo = queryDataToCache(cid,page,limit);
                return returnBo;
            } catch (JsonProcessingException je) {
                LOGGER.error(je.getMessage());
            }
        }
        return returnBo;
    }

    @Override
    public PageInfo<CommentVo> getCommentsWithPage(CommentVoExample commentVoExample, int page, int limit) {
        PageHelper.startPage(page, limit);
        List<CommentVo> commentVos = commentDao.selectByExampleWithBLOBs(commentVoExample);
        PageInfo<CommentVo> pageInfo = new PageInfo<>(commentVos);
        return pageInfo;
    }

    @Override
    public void update(CommentVo comments) {
        if (null != comments && null != comments.getCoid()) {
            commentDao.updateByPrimaryKeyWithBLOBs(comments);
        }
    }

    @Override
    public void delete(Integer coid, Integer cid) {
        if (null == coid) {
            throw new TipException("主键为空");
        }
        commentDao.deleteByPrimaryKey(coid);
        redisStringCache.deleteCache(cid+COMMENTS);
        ContentVo contents = contentService.getContents(cid + "");
        if (null != contents && contents.getCommentsNum() > 0) {
            ContentVo temp = new ContentVo();
            temp.setCid(cid);
            temp.setCommentsNum(contents.getCommentsNum() - 1);
            contentService.updateContentByCid(temp);
        }
    }

    @Override
    public CommentVo getCommentById(Integer coid) {
        if (null != coid) {
            return commentDao.selectByPrimaryKey(coid);
        }
        return null;
    }

    /**
     * copy原有的分页信息，除数据
     *
     * @param ordinal
     * @param <T>
     * @return
     */
    private <T> PageInfo<T> copyPageInfo(PageInfo ordinal) {
        PageInfo<T> returnBo = new PageInfo<T>();
        returnBo.setPageSize(ordinal.getPageSize());
        returnBo.setPageNum(ordinal.getPageNum());
        returnBo.setEndRow(ordinal.getEndRow());
        returnBo.setTotal(ordinal.getTotal());
        returnBo.setHasNextPage(ordinal.isHasNextPage());
        returnBo.setHasPreviousPage(ordinal.isHasPreviousPage());
        returnBo.setIsFirstPage(ordinal.isIsFirstPage());
        returnBo.setIsLastPage(ordinal.isIsLastPage());
        returnBo.setNavigateFirstPage(ordinal.getNavigateFirstPage());
        returnBo.setNavigateLastPage(ordinal.getNavigateLastPage());
        returnBo.setNavigatepageNums(ordinal.getNavigatepageNums());
        returnBo.setSize(ordinal.getSize());
        returnBo.setPrePage(ordinal.getPrePage());
        returnBo.setNextPage(ordinal.getNextPage());
        return returnBo;
    }

    /**
     * 从数据库读取评论
     * @param cid
     * @param page
     * @param limit
     * @return 返回一个包装过的评论PageInfo
     * @throws JsonProcessingException
     */
    private PageInfo<CommentBo> queryDataToCache(Integer cid, int page, int limit) throws JsonProcessingException {
        String s = null;
        PageHelper.startPage(page, limit);
        CommentVoExample commentVoExample = new CommentVoExample();
        commentVoExample.createCriteria().andCidEqualTo(cid).andParentEqualTo(0);
        commentVoExample.setOrderByClause("coid desc");
        List<CommentVo> parents = commentDao.selectByExampleWithBLOBs(commentVoExample);
        PageInfo<CommentVo> commentPaginator = new PageInfo<>(parents);
        PageInfo<CommentBo> returnBo = copyPageInfo(commentPaginator);
        if (parents.size() != 0) {
            List<CommentBo> comments = new ArrayList<>(parents.size());
            parents.forEach(parent -> {
                CommentBo comment = new CommentBo(parent);
                comments.add(comment);
            });
            returnBo.setList(comments);
        }
        return returnBo;
    }
}

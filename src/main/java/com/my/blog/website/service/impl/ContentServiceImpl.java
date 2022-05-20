package com.my.blog.website.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.my.blog.website.cache.StringCache;
import com.my.blog.website.constant.WebConst;
import com.my.blog.website.dao.ContentVoMapper;
import com.my.blog.website.dao.MetaVoMapper;
import com.my.blog.website.dto.Types;
import com.my.blog.website.exception.TipException;
import com.my.blog.website.model.Vo.ContentVo;
import com.my.blog.website.model.Vo.ContentVoExample;
import com.my.blog.website.service.IContentService;
import com.my.blog.website.service.IMetaService;
import com.my.blog.website.service.IRelationshipService;
import com.my.blog.website.utils.DateKit;
import com.my.blog.website.utils.JsonUtil;
import com.my.blog.website.utils.TaleUtils;
import com.my.blog.website.utils.Tools;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2021/3/13 013.
 */
@Service
public class ContentServiceImpl implements IContentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Resource
    private ContentVoMapper contentDao;

    @Resource
    private MetaVoMapper metaDao;

    @Resource
    private IRelationshipService relationshipService;

    @Resource
    private IMetaService metasService;

    @Resource
    private StringCache redisStringCache;

    @Override
    public void publish(ContentVo contents) {
        if (null == contents) {
            throw new TipException("文章对象为空");
        }
        if (StringUtils.isBlank(contents.getTitle())) {
            throw new TipException("文章标题不能为空");
        }
        if (StringUtils.isBlank(contents.getContent())) {
            throw new TipException("文章内容不能为空");
        }
        int titleLength = contents.getTitle().length();
        if (titleLength > WebConst.MAX_TITLE_COUNT) {
            throw new TipException("文章标题过长");
        }
        int contentLength = contents.getContent().length();
        if (contentLength > WebConst.MAX_TEXT_COUNT) {
            throw new TipException("文章内容过长");
        }
        if (null == contents.getAuthorId()) {
            throw new TipException("请登录后发布文章");
        }
        if (StringUtils.isNotBlank(contents.getSlug())) {
            if (contents.getSlug().length() < 5) {
                throw new TipException("路径太短了");
            }
            if (!TaleUtils.isPath(contents.getSlug())) {throw new TipException("您输入的路径不合法");}
            ContentVoExample contentVoExample = new ContentVoExample();
            contentVoExample.createCriteria().andTypeEqualTo(contents.getType()).andStatusEqualTo(contents.getSlug());
            long count = contentDao.countByExample(contentVoExample);
            if (count > 0) {throw new TipException("该路径已经存在，请重新输入");}
        } else {
            contents.setSlug(null);
        }

        contents.setContent(EmojiParser.parseToAliases(contents.getContent()));

        int time = DateKit.getCurrentUnixTime();
        contents.setCreated(time);
        contents.setModified(time);
        contents.setHits(0);
        contents.setCommentsNum(0);

        String tags = contents.getTags();
        String categories = contents.getCategories();
        contentDao.insert(contents);
        Integer cid = contents.getCid();

        metasService.saveMetas(cid, tags, Types.TAG.getType());
        metasService.saveMetas(cid, categories, Types.CATEGORY.getType());
    }

    @Override
    public PageInfo<ContentVo> getContents(Integer p, Integer limit) {
        LOGGER.debug("Enter getContents method");
        ContentVoExample example = new ContentVoExample();
        example.setOrderByClause("created desc");
        example.createCriteria().andTypeEqualTo(Types.ARTICLE.getType()).andStatusEqualTo(Types.PUBLISH.getType());
        PageHelper.startPage(p, limit);
        List<ContentVo> data = contentDao.selectByExampleWithBLOBs(example);
        PageInfo<ContentVo> pageInfo = new PageInfo<>(data);
        LOGGER.debug("Exit getContents method");
        return pageInfo;
    }

    @Override
    public ContentVo getContents(String id) {
        ContentVo contentVo;
        if (StringUtils.isNotBlank(id)) {
            try{
                String content = redisStringCache.getCache(id);
                if (StringUtils.isNotBlank(content)){
                    if ( (contentVo = (JsonUtil.string2Obj(content,ContentVo.class))) != null ){
                        contentVo.setHits(contentVo.getHits()+1);
                        return contentVo;
                    }
                }else {
                    if (Tools.isNumber(id)) {
                        contentVo = contentDao.selectByPrimaryKey(Integer.valueOf(id));
                        boolean success = updateHits(contentVo);
                        if ( !success ){ LOGGER.error("article reading data update failed"); }
                        // 将文章缓存至redis
                        redisStringCache.addCacheByTime(id,JsonUtil.obj2String(contentVo),7, TimeUnit.DAYS);
                        return contentVo;
                    } else {
                        ContentVoExample contentVoExample = new ContentVoExample();
                        contentVoExample.createCriteria().andSlugEqualTo(id);
                        List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(contentVoExample);
                        if (contentVos.isEmpty()){
                            return null;
                        }
                        redisStringCache.addCacheByTime(id,JsonUtil.obj2String(contentVos.get(0)),7,TimeUnit.DAYS);
                        if (contentVos.size() != 1) {
                            throw new TipException("query content by id and return is not one");
                        }
                        return contentVos.get(0);
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            } catch (RedisConnectionFailureException e){
                LOGGER.error(e.getMessage());
                if (StringUtils.isNotBlank(id)){
                    if (Tools.isNumber(id)) {
                        contentVo = contentDao.selectByPrimaryKey(Integer.valueOf(id));
                        boolean success = updateHits(contentVo);
                        if ( !success ){ LOGGER.error("article reading data update failed"); }
                        return contentVo;
                    } else {
                        ContentVoExample contentVoExample = new ContentVoExample();
                        contentVoExample.createCriteria().andSlugEqualTo(id);
                        List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(contentVoExample);
                        if (contentVos.isEmpty()){
                            return null;
                        }
                        if (contentVos.size() != 1) {
                            throw new TipException("query content by id and return is not one");
                        }
                        return contentVos.get(0);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void updateContentByCid(ContentVo contentVo) {
        if (null != contentVo && null != contentVo.getCid()) {
            contentDao.updateByPrimaryKeySelective(contentVo);
        }
    }

    @Override
    public PageInfo<ContentVo> getArticles(Integer mid, int page, int limit) {
        int total = metaDao.countWithSql(mid);
        PageHelper.startPage(page, limit);
        List<ContentVo> list = contentDao.findByCatalog(mid);
        PageInfo<ContentVo> paginator = new PageInfo<>(list);
        paginator.setTotal(total);
        return paginator;
    }

    @Override
    public PageInfo<ContentVo> getArticles(String keyword, Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        ContentVoExample contentVoExample = new ContentVoExample();
        ContentVoExample.Criteria criteria = contentVoExample.createCriteria();
        criteria.andTypeEqualTo(Types.ARTICLE.getType());
        criteria.andStatusEqualTo(Types.PUBLISH.getType());
        criteria.andTitleLike("%" + keyword + "%");
        contentVoExample.setOrderByClause("created desc");
        List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(contentVoExample);
        return new PageInfo<>(contentVos);
    }

    @Override
    public PageInfo<ContentVo> getArticlesWithpage(ContentVoExample commentVoExample, Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(commentVoExample);
        return new PageInfo<>(contentVos);
    }

    @Override
    public void deleteByCid(Integer cid) {
        ContentVo contents = this.getContents(cid + "");
        if (null != contents) {
            contentDao.deleteByPrimaryKey(cid);
            relationshipService.deleteById(cid, null);
        }
    }

    @Override
    public void updateCategory(String ordinal, String newCatefory) {
        ContentVo contentVo = new ContentVo();
        contentVo.setCategories(newCatefory);
        ContentVoExample example = new ContentVoExample();
        example.createCriteria().andCategoriesEqualTo(ordinal);
        contentDao.updateByExampleSelective(contentVo, example);
    }

    @Override
    public int updateArticle(ContentVo contents) {
        if (null == contents || null == contents.getCid()) {
            throw new TipException("文章对象不能为空");
        }
        if (StringUtils.isBlank(contents.getTitle())) {
            throw new TipException("文章标题不能为空");
        }
        if (StringUtils.isBlank(contents.getContent())) {
            throw new TipException("文章内容不能为空");
        }
        if (contents.getTitle().length() > 200) {
            throw new TipException("文章标题过长");
        }
        if (contents.getContent().length() > 65000) {
            throw new TipException("文章内容过长");
        }
        if (null == contents.getAuthorId()) {
            throw new TipException("请登录后发布文章");
        }
        if (StringUtils.isBlank(contents.getSlug())) {
            contents.setSlug(null);
        }
        int time = DateKit.getCurrentUnixTime();
        contents.setModified(time);
        Integer cid = contents.getCid();
        contents.setContent(EmojiParser.parseToAliases(contents.getContent()));

        int i = contentDao.updateByPrimaryKeySelective(contents);
        relationshipService.deleteById(cid, null);
        metasService.saveMetas(cid, contents.getTags(), Types.TAG.getType());
        metasService.saveMetas(cid, contents.getCategories(), Types.CATEGORY.getType());
        return i;
    }

    /**
     * 更新点击数
     * @param contentVo
     * @return
     */
    private boolean updateHits(ContentVo contentVo){
        if (contentVo != null) {
            contentVo.setHits(contentVo.getHits() + 1);
            int i = contentDao.updateByPrimaryKey(contentVo);
            if (i == 1){
               return true;
            }
        }
        return false;
    }


}

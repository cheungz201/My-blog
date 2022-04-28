#simple Dockerfile
FROM java:8
ADD blog.jar /usr/local/blog.jar
EXPOSE 80
ENTRYPOINT ["java","-jar","/usr/local/blog.jar"]
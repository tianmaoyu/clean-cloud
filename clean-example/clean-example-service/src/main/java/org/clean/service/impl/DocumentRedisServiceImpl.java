package org.clean.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.clean.service.DocumentRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DocumentRedisServiceImpl implements DocumentRedisService {

    @Autowired
    private  RedisTemplate<String, byte[]> redisTemplate;
    
    // Redis key 前缀
    private static final String DOCUMENT_KEY_PREFIX = "doc:word:";
    
    // 默认过期时间：7天
    private static final long DEFAULT_EXPIRE_HOURS = 7 * 24;


    /**
     * 保存 Word 文档到 Redis（带过期时间）
     * 
     * @param documentId 文档ID
     * @param document Word 文档对象
     * @return 是否保存成功
     */
    public boolean saveDocument(String documentId, XWPFDocument document) {
        String redisKey = buildDocumentKey(documentId);
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 将文档写入字节数组
            document.write(outputStream);
            byte[] documentBytes = outputStream.toByteArray();
            
            // 保存到 Redis
            redisTemplate.opsForValue().set(redisKey, documentBytes, 24, TimeUnit.HOURS);
            
            log.info("文档保存成功，ID: {}, 大小: {} bytes", documentId, documentBytes.length);
            return true;
            
        } catch (IOException e) {
            log.error("保存文档到Redis失败，ID: {}", documentId, e);
            return false;
        }
    }

    /**
     * 保存文档字节数组到 Redis
     * 
     * @param documentId 文档ID
     * @param documentBytes 文档字节数组
     * @return 是否保存成功
     */
    public boolean saveDocumentBytes(String documentId, byte[] documentBytes) {
        return saveDocumentBytes(documentId, documentBytes, DEFAULT_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    /**
     * 保存文档字节数组到 Redis（带过期时间）
     * 
     * @param documentId 文档ID
     * @param documentBytes 文档字节数组
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     * @return 是否保存成功
     */
    public boolean saveDocumentBytes(String documentId, byte[] documentBytes, long timeout, TimeUnit timeUnit) {
        String redisKey = buildDocumentKey(documentId);
        
        try {
            redisTemplate.opsForValue().set(redisKey, documentBytes, timeout, timeUnit);
            log.info("文档字节数组保存成功，ID: {}, 大小: {} bytes", documentId, documentBytes.length);
            return true;
        } catch (Exception e) {
            log.error("保存文档字节数组到Redis失败，ID: {}", documentId, e);
            return false;
        }
    }

    /**
     * 从 Redis 获取 Word 文档
     * 
     * @param documentId 文档ID
     * @return Word 文档对象，如果不存在返回 null
     */
    public XWPFDocument getDocument(String documentId) {
        String redisKey = buildDocumentKey(documentId);
        
        try {
            byte[] documentBytes = redisTemplate.opsForValue().get(redisKey);
            if (documentBytes == null) {
                log.info("文档不存在，ID: {}", documentId);
                return null;
            }
            
            XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(documentBytes));
            log.info("文档获取成功，ID: {}, 大小: {} bytes", documentId, documentBytes.length);
            return document;
            
        } catch (IOException e) {
            log.error("从Redis获取文档失败，ID: {}", documentId, e);
            return null;
        } catch (Exception e) {
            log.error("处理Redis文档数据失败，ID: {}", documentId, e);
            return null;
        }
    }

    /**
     * 从 Redis 获取文档字节数组
     * 
     * @param documentId 文档ID
     * @return 文档字节数组，如果不存在返回 null
     */
    public byte[] getDocumentBytes(String documentId) {
        String redisKey = buildDocumentKey(documentId);
        
        try {
            byte[] documentBytes = redisTemplate.opsForValue().get(redisKey);
            if (documentBytes != null) {
                log.info("文档字节数组获取成功，ID: {}, 大小: {} bytes", documentId, documentBytes.length);
            } else {
                log.info("文档字节数组不存在，ID: {}", documentId);
            }
            return documentBytes;
        } catch (Exception e) {
            log.error("从Redis获取文档字节数组失败，ID: {}", documentId, e);
            return null;
        }
    }

    /**
     * 检查文档是否存在
     * 
     * @param documentId 文档ID
     * @return 是否存在
     */
    public boolean exists(String documentId) {
        String redisKey = buildDocumentKey(documentId);
        Boolean exists = redisTemplate.hasKey(redisKey);
        return exists != null && exists;
    }

    /**
     * 删除文档
     * 
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    public boolean deleteDocument(String documentId) {
        String redisKey = buildDocumentKey(documentId);
        Boolean deleted = redisTemplate.delete(redisKey);
        boolean success = deleted != null && deleted;
        
        if (success) {
            log.info("文档删除成功，ID: {}", documentId);
        } else {
            log.info("文档删除失败或不存在，ID: {}", documentId);
        }
        
        return success;
    }

    /**
     * 获取文档剩余过期时间
     * 
     * @param documentId 文档ID
     * @param timeUnit 时间单位
     * @return 剩余时间，如果不存在返回 -2，永不过期返回 -1
     */
    public long getExpire(String documentId, TimeUnit timeUnit) {
        String redisKey = buildDocumentKey(documentId);
        Long expire = redisTemplate.getExpire(redisKey, timeUnit);
        return expire == null ? -2 : expire;
    }

    /**
     * 更新文档过期时间
     * 
     * @param documentId 文档ID
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     * @return 是否更新成功
     */
    public boolean updateExpire(String documentId, long timeout, TimeUnit timeUnit) {
        String redisKey = buildDocumentKey(documentId);
        Boolean result = redisTemplate.expire(redisKey, timeout, timeUnit);
        return result != null && result;
    }

    /**
     * 构建 Redis key
     */
    private String buildDocumentKey(String documentId) {
        return DOCUMENT_KEY_PREFIX + documentId;
    }
}
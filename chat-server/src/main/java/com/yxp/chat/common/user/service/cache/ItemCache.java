package com.yxp.chat.common.user.service.cache;

import com.yxp.chat.common.user.dao.ItemConfigDao;
import com.yxp.chat.common.user.domain.entity.ItemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Description: 物品相关缓存

 */
@Component
public class ItemCache {//todo 多级缓存

    @Autowired
    private ItemConfigDao itemConfigDao;

    @Cacheable(cacheNames = "item", key = "'itemsByType:'+#itemType")
    public List<ItemConfig> getByType(Integer itemType) {
        return itemConfigDao.getByType(itemType);
    }

    /**
     * 清空缓存
     * @param itemType
     */
    @CacheEvict(cacheNames = "item", key = "'itemsByType:'+#itemType")
    public void evictByType(Integer itemType){
    }

    @Cacheable(cacheNames = "item", key = "'item:'+#itemId")
    public ItemConfig getById(Long itemId) {
        return itemConfigDao.getById(itemId);
    }
}

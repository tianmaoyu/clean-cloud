package org.clean.system.service.impl;

import org.clean.system.entity.TransactionMessage;
import org.clean.system.mapper.TransactionMessageMapper;
import org.clean.system.service.TransactionMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class TransactionMessageServiceImpl implements TransactionMessageService {

    @Autowired
    private TransactionMessageMapper transactionMessageMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private PlatformTransactionManager transactionManager;


    @Override
    @Transactional
    public void save(TransactionMessage message) {
       transactionMessageMapper.insert(message);
       eventPublisher.publishEvent(message);
    }

    @Override
    public void save2(TransactionMessage message) {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try{
            transactionMessageMapper.insert(message);

            //手动事务- 必须要在提 commit 前(原理 提前注册)
            eventPublisher.publishEvent(message);

            transactionManager.commit(status);

        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }

    }

    @Override
    public void update(TransactionMessage message) {

    }

}

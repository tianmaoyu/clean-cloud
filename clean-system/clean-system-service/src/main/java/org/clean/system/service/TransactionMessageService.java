package org.clean.system.service;

import org.clean.system.entity.TransactionMessage;

public interface TransactionMessageService {
    void save(TransactionMessage message);
    void save2(TransactionMessage message);
    void update(TransactionMessage message);
}

package org.clean.system.service;

import java.io.IOException;

public interface DemoService {
   public void doSomething();

    public void doSomethingElse();

    public byte[] excelReport(Integer id) throws IOException;
}

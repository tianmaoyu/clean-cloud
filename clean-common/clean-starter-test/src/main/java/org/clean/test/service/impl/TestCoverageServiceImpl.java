package org.clean.test.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.clean.test.entity.TestCoverage;
import org.clean.test.service.TestCoverageService;
import org.clean.test.mapper.TestCoverageMapper;
import org.springframework.stereotype.Service;

/**
* @author eric
* @description 针对表【test_coverage】的数据库操作Service实现
* @createDate 2025-12-06 16:57:29
*/
@Service
public class TestCoverageServiceImpl extends ServiceImpl<TestCoverageMapper, TestCoverage>
    implements TestCoverageService{

}





package com.pineapple.veritas;

import com.pineapple.veritas.mapper.RecordMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration
public class VeritasServiceTests {

  @MockBean
  private RecordMapper recordMapper;

  @MockBean
  private SqlSessionTemplate sqlSessionTemplate;


}

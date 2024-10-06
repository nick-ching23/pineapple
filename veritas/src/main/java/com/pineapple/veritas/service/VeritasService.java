package com.pineapple.veritas.service;

import com.pineapple.veritas.entity.Record;
import com.pineapple.veritas.mapper.RecordMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

public class VeritasService {
  @Autowired
  RecordMapper recordMapper;

  public ResponseEntity<?> checkText(String text) {
    //This should be calling the OpenAI API, change return val
    return new ResponseEntity<>(null, HttpStatus.OK);
  }

  public ResponseEntity<?> checkTextUser(String text, String userID, String orgID) {

    ResponseEntity<Integer> textRes = (ResponseEntity<Integer>) checkText(text);
    //this will probably not be an int
    Integer flagged = textRes.getBody();
    int threshold = 0; //change this later

    Map<String, Object> recordMap = new HashMap<>();
    recordMap.put("orgID", orgID);
    recordMap.put("userID", userID);
    List<Record> records = recordMapper.selectByMap(recordMap);
    if (records.isEmpty()) {
      Record record = new Record();
      record.setOrgID(orgID);
      record.setUserID(userID);
      if (flagged > threshold) {
        record.setNumFlags(1);
      } else {
        record.setNumFlags(0);
      }
      recordMapper.insert(record);
    } else if (flagged > threshold) {
      Record record = records.get(0);
      record.setNumFlags(record.getNumFlags() + 1);
      recordMapper.updateById(record);
    }
    //change return val
    return new ResponseEntity<>(null, HttpStatus.OK);
  }

  public ResponseEntity<?> numFlags(String userID, String orgID) {
    Map<String, Object> recordMap = new HashMap<>();
    recordMap.put("orgID", orgID);
    recordMap.put("userID", userID);
    List<Record> records = recordMapper.selectByMap(recordMap);
    if (records.isEmpty()) {
      return new ResponseEntity<>(0, HttpStatus.OK);
    } else {
      Record record = records.get(0);
      return new ResponseEntity<>(record.getNumFlags(), HttpStatus.OK);
    }
  }
}

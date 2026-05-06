package com.epam.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/traces")
class TracesController {

  @Autowired
  private TraceService traceService;

  @GetMapping
  List<TraceRecord> getTraces() {
    return traceService.getAll();
  }

  @DeleteMapping
  void clearTraces() {
    traceService.clear();
  }
}

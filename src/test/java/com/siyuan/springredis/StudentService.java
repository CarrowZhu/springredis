package com.siyuan.springredis;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.siyuan.springredis.annotation.SpringRedisConfig;
import com.siyuan.springredis.annotation.SpringRedisHashCache;
import com.siyuan.springredis.annotation.SpringRedisHashEvict;
import com.siyuan.springredis.annotation.SpringRedisValueCache;
import com.siyuan.springredis.annotation.SpringRedisValueEvict;

@Service("studentService")
@SpringRedisConfig("studentRedisTemplate")
public class StudentService {
	
	private StudentDAO studentDAO;
	
	@SpringRedisValueCache(key = "'student:' + #id", condition = "#id > 100", 
			timeout = 60, timeUnit = TimeUnit.MINUTES, refreshTTL = true)
	public Student getById(long id) {
		return studentDAO.getById(id);
	}
	
	@SpringRedisValueEvict(key = "'student:' + #student.id", condition = "#student.id > 100")
	public void updateStudent(Student student) {
		studentDAO.updateStudent(student);
	}
	
	@SpringRedisHashCache(key = "'students'", hashKey = "#id.toString()", condition = "#id > 100", 
			timeout = 60, timeUnit = TimeUnit.MINUTES, refreshTTL = true)
	public Student getById2(long id) {
		return studentDAO.getById(id);
	}
	
	@SpringRedisHashEvict(key = "'students'", hashKey = "#student.id.toString()", condition = "#student.id > 100")
	public void updateStudent2(Student student) {
		studentDAO.updateStudent(student);
	}
	
	public StudentDAO getStudentDAO() {
		return studentDAO;
	}

	public void setStudentDAO(StudentDAO studentDAO) {
		this.studentDAO = studentDAO;
	}
	
}

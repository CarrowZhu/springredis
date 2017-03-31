package com.siyuan.springredis;

public interface StudentDAO {
	
	Student getById(long id);
	
	void updateStudent(Student student);
	
}

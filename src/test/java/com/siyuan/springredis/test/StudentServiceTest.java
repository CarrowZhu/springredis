package com.siyuan.springredis.test;

import org.junit.After;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.siyuan.springredis.Student;
import com.siyuan.springredis.StudentDAO;
import com.siyuan.springredis.StudentService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/ApplicationContext-SpringRedis.xml")
public class StudentServiceTest {
	
	@Autowired
	@Qualifier("studentRedisTemplate")
	private RedisTemplate<String, Student> redisTemplate;
	
	@Autowired
	private StudentService service;
	
	private StudentDAO studentDAO;
	
	private StudentDAO mock;
	
	@Before
	public void setUp() {
		redisTemplate.delete("student:123");
		redisTemplate.delete("student:100");
		redisTemplate.delete("students");
		studentDAO = service.getStudentDAO();
		mock = mock(StudentDAO.class);
		service.setStudentDAO(mock);
	}
	
	@Test
	public void testGetById() {
		when(mock.getById(123L)).thenReturn(new Student(123L, "name:123"));
		// no cache
		Student stu = service.getById(123L);
		assertArrayEquals(new Object[] { new Student(123L, "name:123") }, new Object[] { stu });
		// cache
		stu = service.getById(123L);
		assertArrayEquals(new Object[] { new Student(123L, "name:123") }, new Object[] { stu });
		verify(mock, times(1)).getById(123L);
		
		when(mock.getById(100L)).thenReturn(new Student(100L, "name:100"));
		// no cache
		stu = service.getById(100L);
		assertArrayEquals(new Object[] { new Student(100L, "name:100") }, new Object[] { stu });
		// no cache
		stu = service.getById(100L);
		assertArrayEquals(new Object[] { new Student(100L, "name:100") }, new Object[] { stu });
		verify(mock, times(2)).getById(100L);
	}
	
	@Test
	public void testUpdateStudent() {
		// evict
		Student stu = new Student(123L, "name:123");
		redisTemplate.opsForValue().set("student:123", stu);
		service.updateStudent(stu);
		assertNull(redisTemplate.opsForValue().get("student:123"));
		
		// do not evict
		stu = new Student(100L, "name:100");
		redisTemplate.opsForValue().set("student:100", stu);
		service.updateStudent(stu);
		assertNotNull(redisTemplate.opsForValue().get("student:100"));
	}
	
	@Test
	public void testGetById2() {
		when(mock.getById(123L)).thenReturn(new Student(123L, "name:123"));
		// no cache
		Student stu = service.getById2(123L);
		assertArrayEquals(new Object[] { new Student(123L, "name:123") }, new Object[] { stu });
		// cache
		stu = service.getById2(123L);
		assertArrayEquals(new Object[] { new Student(123L, "name:123") }, new Object[] { stu });
		verify(mock, times(1)).getById(123L);
		
		when(mock.getById(100L)).thenReturn(new Student(100L, "name:100"));
		// no cache
		stu = service.getById2(100L);
		assertArrayEquals(new Object[] { new Student(100L, "name:100") }, new Object[] { stu });
		// no cache
		stu = service.getById2(100L);
		assertArrayEquals(new Object[] { new Student(100L, "name:100") }, new Object[] { stu });
		verify(mock, times(2)).getById(100L);
	}
	
	@Test
	public void testUpdateStudent2() {
		// evict
		Student stu = new Student(123L, "name:123");
		redisTemplate.opsForHash().put("students", "123", stu);
		service.updateStudent2(stu);
		assertNull(redisTemplate.opsForHash().get("students", "123"));
		
		// do not evict
		stu = new Student(100L, "name:100");
		redisTemplate.opsForHash().put("students", "100", stu);
		service.updateStudent2(stu);
		assertNotNull(redisTemplate.opsForHash().get("students", "100"));
	}
	
	@After
	public void clear() {
		service.setStudentDAO(studentDAO);
	}

}

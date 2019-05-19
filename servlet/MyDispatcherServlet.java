package com.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.annotation.MyAutowired;
import com.annotation.MyController;
import com.annotation.MyService;

public class MyDispatcherServlet extends HttpServlet {

	private Logger logger = Logger.getLogger("init");

	private Properties properties = new Properties();

	private List<String> classNames = new ArrayList<>();
	private Map<String, Object> ioc = new HashMap<>();

	private Map<String, Method> handlerMapping = new HashMap<>();

	private Map<String, Object> controllerMap = new HashMap<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init();
		logger.info("初始化MyDispatcherServlet");
		// 1.加载配置文件，填充properties字段；
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		// 2.根据properties，初始化所有相关联的类,扫描用户设定的包下面所有的类
		doScanner(properties.getProperty("scanPackage"));

		// 3.拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v beanName-bean) beanName默认是首字母小写
		doInstance();

		// 4.自动化注入依赖
		doAutowired();

		// 5.初始化HandlerMapping(将url和method对应上)
		initHandlerMapping();

		doAutowired2();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 注释掉父类实现，不然会报错：405 HTTP method GET is not supported by this URL
		// super.doPost(req, resp);
		logger.info("执行MyDispatcherServlet的doPost()");
		try {
			// 处理请求
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter().write("500!! Server Exception");
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 注释掉父类实现，不然会报错：405 HTTP method GET is not supported by this URL
		// super.doGet(req, resp);
		logger.info("执行MyDispatcherServlet的doGet()");
		try {
			// 处理请求
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter().write("500!! Server Exception");
		}
	}

	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
		// TODO Auto-generated method stub

	}

	private void doAutowired2() {
		// TODO Auto-generated method stub

	}

	private void initHandlerMapping() {
		// TODO Auto-generated method stub
		if (ioc.isEmpty()) {
			return;
		}
	}

	private void doAutowired() {
		// TODO Auto-generated method stub
		if (ioc.isEmpty()) {
			return;
		}
		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				if (!field.isAnnotationPresent(MyAutowired.class)) {
					continue;
				}
				MyAutowired autowired = field.getAnnotation(MyAutowired.class);
				String beanName = autowired.value().trim();
				if ("".equals(beanName)) {
					beanName = field.getType().getName();
				}
				field.setAccessible(true);
				try {
					field.set(entry.getValue(), ioc.get(beanName));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

			}

		}

	}

	private void doInstance() {
		// TODO Auto-generated method stub
		if (classNames.isEmpty()) {
			return;
		}
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className);
				if (clazz.isAnnotationPresent(MyController.class)) {
					ioc.put(toLowerFirstWord(className), clazz.newInstance());
				} else if (clazz.isAnnotationPresent(MyService.class)) {
					MyService myService = clazz.getAnnotation(MyService.class);
					String beanName = myService.value();
					if ("".equals(beanName.trim())) {
						beanName = toLowerFirstWord(clazz.getSimpleName());
					}
					Object instance = clazz.newInstance();
					ioc.put(beanName, instance);
					Class[] interfaces = clazz.getInterfaces();
					for (Class<?> i : interfaces) {
						ioc.put(i.getName(), instance);
					}
				} else {
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private String toLowerFirstWord(String name) {

		char[] charArray = name.toCharArray();
		charArray[0] += 32;
		return String.valueOf(charArray);
	}

	private void doScanner(String packageName) {
		// TODO Auto-generated method stub
		URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				doScanner(packageName + "." + file.getName());
			} else {
				String className = packageName + file.getName().replace(".class", "");
				System.out.println("类名 ：" + className);
				classNames.add(className);
			}
		}
	}

	private void doLoadConfig(String location) {
		// TODO Auto-generated method stub
		InputStream inputStream = this.getClass().getResourceAsStream(location);
		try {
			logger.info("读取" + location + "文件的内容");
			properties.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

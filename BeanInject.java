package ztj.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 属性注入类 [属性复制:0 | 填充注入:1 | 过滤注入:2 | 更新注入:3]
 * 
 * @author Timothy
 * @version 3.0
 * @date 2011-12-16
 */
public class BeanInject {

	/** 时间格式化（日期 时间） */
	private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/** 时间格式化（日期） */
	private static final DateFormat DF_DATE = new SimpleDateFormat("yyyy-MM-dd");

	/** 时间格式化（时间） */
	private static final DateFormat DF_TIME = new SimpleDateFormat("HH:mm:ss");

	/** 字段名列表 */
	private List<String> fieldNameList;

	/** 注入模式 */
	private int injectMode = 0;

	/** 是否排除零（基本数值类型为0的，当成对象null对待） */
	private boolean excludeZero = false;

	public BeanInject() {
		fieldNameList = new ArrayList<String>();
	}

	public BeanInject(boolean excludeZero) {
		this.excludeZero = excludeZero;
	}

	public static BeanInject getInstance() {
		return new BeanInject();
	}

	public static BeanInject getInstance(boolean excludeZero) {
		return new BeanInject(excludeZero);
	}

	/**
	 * 属性复制
	 * 
	 * @param sourceObj
	 *            源对象
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	public void copyProperties(Object sourceObj, Object targetObj) {
		injectMode = 0;
		inject(sourceObj, targetObj);
	}

	/**
	 * 填充注入
	 * 
	 * @param sourceObj
	 *            源对象
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	public void fillInject(Object sourceObj, Object targetObj) {
		injectMode = 1;
		inject(sourceObj, targetObj);
	}

	/**
	 * 过滤注入
	 * 
	 * @param sourceObj
	 *            源对象
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	public void filterInject(Object sourceObj, Object targetObj) {
		injectMode = 2;
		inject(sourceObj, targetObj);
	}

	/**
	 * 更新注入
	 * 
	 * @param sourceObj
	 *            源对象
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	public void updateInject(Object sourceObj, Object targetObj) {
		injectMode = 3;
		inject(sourceObj, targetObj);
	}

	/**
	 * 属性复制
	 * 
	 * @param sourceMap
	 *            源Map
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	public void copyProperties(Map<String, String> sourceMap, Object targetObj) {
		injectMode = 0;
		inject(sourceMap, targetObj);
	}

	/**
	 * 填充注入
	 * 
	 * @param sourceMap
	 *            源Map
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	public void fillInject(Map<String, String> sourceMap, Object targetObj) {
		injectMode = 1;
		inject(sourceMap, targetObj);
	}

	/**
	 * 过滤注入
	 * 
	 * @param sourceMap
	 *            源Map
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	public void filterInject(Map<String, String> sourceMap, Object targetObj) {
		injectMode = 2;
		inject(sourceMap, targetObj);
	}

	/**
	 * 更新注入
	 * 
	 * @param sourceMap
	 *            源Map
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	public void updateInject(Map<String, String> sourceMap, Object targetObj) {
		injectMode = 3;
		inject(sourceMap, targetObj);
	}

	/**
	 * Bean属性注入
	 * 
	 * @param sourceObj
	 *            源对象
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	private void inject(Object sourceObj, Object targetObj) {
		if (sourceObj == null || targetObj == null) {
			return;
		}
		// 获取源对象的类对象
		Class sourceClass = sourceObj.getClass();
		// 遍历包括父类的所有属性
		traceAllField(sourceClass);
		// 获取目标对象的类对象
		Class targetClass = targetObj.getClass();
		// 循环注入传值
		for (String fieldName : fieldNameList) {
			// 获取Field对象
			Field sourceField = traceFieldByName(sourceClass, fieldName);
			Field targetField = traceFieldByName(targetClass, fieldName);
			if (targetField == null || sourceField == null) {
				continue;
			}
			// 属性类型
			Class sourceFieldType = sourceField.getType();
			Class targetFieldType = targetField.getType();
			try {
				// 获取get方法
				String getMethodName = buildMethodName(fieldName, "get");
				Method sourceGetMethod = sourceField.getDeclaringClass()
						.getMethod(getMethodName);
				Method targetGetMethod = targetField.getDeclaringClass()
						.getMethod(getMethodName);
				// 获取字段值
				Object sourceValue = sourceGetMethod.invoke(sourceObj);
				Object targetValue = targetGetMethod.invoke(targetObj);
				// 排除零
				if (excludeZero) {
					switch (injectMode) {
					case 1: // 填充注入
						if ((targetValue != null && !checkZero(targetValue,
								targetFieldType))
								|| (sourceValue == null && checkZero(
										sourceValue, sourceFieldType))) {
							continue;
						}
						break;
					case 2: // 过滤注入
						if ((sourceValue == null || "".equals(sourceValue))
								&& checkZero(sourceValue, sourceFieldType)) {
							continue;
						}
						break;
					case 3: // 更新注入
						if ((sourceValue == null && checkZero(sourceValue,
								sourceFieldType))
								|| (targetValue == null && checkZero(
										targetValue, targetFieldType))) {
							continue;
						}
						break;
					default:
						break;
					}
				}
				// 不排除零
				else {
					switch (injectMode) {
					case 1: // 填充注入
						if (targetValue != null || sourceValue == null) {
							continue;
						}
						break;
					case 2: // 过滤注入
						if (sourceValue == null || "".equals(sourceValue)) {
							continue;
						}
						break;
					case 3: // 更新注入
						if (sourceValue == null || targetValue == null) {
							continue;
						}
						break;
					default:
						break;
					}
				}
				// 获取set方法
				String setMethodName = buildMethodName(fieldName, "set");
				Method setTargetMethod = targetClass.getMethod(setMethodName,
						targetFieldType);
				// 传值
				targetValue = sourceValue;
				// 属性类型不一致问题
				if (!sourceFieldType.equals(targetFieldType)) {
					targetValue = convertValue(sourceValue, sourceFieldType,
							targetFieldType);
				}
				// 调用set方法传值
				setTargetMethod.invoke(targetObj, targetValue);
			} catch (SecurityException e) {
				// 安全异常
			} catch (NoSuchMethodException e) {
				// 没有子方法异常
			} catch (IllegalArgumentException e) {
				// 参数异常
			} catch (IllegalAccessException e) {
				// 访问权限异常
			} catch (InvocationTargetException e) {
				// 目标异常
			} catch (ParseException e) {
				// 类型转换异常
				System.err.println(e.toString());
			}
		}
	}

	/**
	 * Bean属性注入
	 * 
	 * @param sourceMap
	 *            源Map
	 * @param targetObj
	 *            目标对象
	 * @throws ParseException
	 */
	private void inject(Map<String, String> sourceMap, Object targetObj) {
		if (sourceMap == null || targetObj == null || sourceMap.isEmpty()) {
			return;
		}
		// 获取目标对象的类对象
		Class targetClass = targetObj.getClass();
		// 遍历Map键
		Set<String> keys = sourceMap.keySet();
		// 循环注入传值
		for (String key : keys) {
			// 获取Field对象
			Field targetField = traceFieldByName(targetClass, key);
			if (targetField == null) {
				continue;
			}
			// 属性类型
			Class targetFieldType = targetField.getType();
			try {
				// 获取get方法
				String getMethodName = buildMethodName(key, "get");
				Method targetGetMethod = targetField.getDeclaringClass()
						.getMethod(getMethodName);
				// 获取字段值
				Object sourceValue = sourceMap.get(key);
				Object targetValue = targetGetMethod.invoke(targetObj);
				// 排除零
				if (excludeZero) {
					switch (injectMode) {
					case 1: // 填充注入
						if ((targetValue != null && !checkZero(targetValue,
								targetFieldType))
								|| (sourceValue == null && checkZero(
										sourceValue, targetFieldType))) {
							continue;
						}
						break;
					case 2: // 过滤注入
						if ((sourceValue == null || "".equals(sourceValue))
								&& checkZero(sourceValue, targetFieldType)) {
							continue;
						}
						break;
					case 3: // 更新注入
						if ((sourceValue == null && checkZero(sourceValue,
								targetFieldType))
								|| (targetValue == null && checkZero(
										targetValue, targetFieldType))) {
							continue;
						}
						break;
					default:
						break;
					}
				}
				// 不排除零
				else {
					switch (injectMode) {
					case 1: // 填充注入
						if (targetValue != null || sourceValue == null) {
							continue;
						}
						break;
					case 2: // 过滤注入
						if (sourceValue == null || "".equals(sourceValue)) {
							continue;
						}
						break;
					case 3: // 更新注入
						if (sourceValue == null || targetValue == null) {
							continue;
						}
						break;
					default:
						break;
					}
				}
				// 获取set方法
				String setMethodName = buildMethodName(key, "set");
				Method setTargetMethod = targetClass.getMethod(setMethodName,
						targetFieldType);
				// 传值
				targetValue = sourceValue;
				// 属性类型不一致问题
				if (!String.class.equals(targetFieldType)) {
					targetValue = convertValue(sourceValue, String.class,
							targetFieldType);
				}
				// 调用set方法传值
				setTargetMethod.invoke(targetObj, targetValue);
			} catch (SecurityException e) {
				// 安全异常
			} catch (NoSuchMethodException e) {
				// 没有子方法异常
			} catch (IllegalArgumentException e) {
				// 参数异常
			} catch (IllegalAccessException e) {
				// 访问权限异常
			} catch (InvocationTargetException e) {
				// 目标异常
			} catch (ParseException e) {
				// 类型转换异常
				System.err.println(e.toString());
			}
		}
	}

	/**
	 * 检查是否为零
	 * 
	 * @param sourceValue
	 *            源值
	 * @param sourceFieldType
	 *            源类型
	 * @return 验证结果
	 */
	public static boolean checkZero(Object sourceValue, Class sourceFieldType) {
		if (sourceValue == null) {
			return false;
		}
		// 源值为数字
		if (sourceFieldType.equals(int.class)) {
			Integer value = (Integer) sourceValue;
			if (value.intValue() == 0) {
				return true;
			}
		} else if (sourceFieldType.equals(long.class)) {
			Long value = (Long) sourceValue;
			if (value.longValue() == 0) {
				return true;
			}
		} else if (sourceFieldType.equals(double.class)) {
			Double value = (Double) sourceValue;
			if (value.doubleValue() == 0) {
				return true;
			}
		} else if (sourceFieldType.equals(short.class)) {
			Short value = (Short) sourceValue;
			if (value.shortValue() == 0) {
				return true;
			}
		} else if (sourceFieldType.equals(float.class)) {
			Float value = (Float) sourceValue;
			if (value.floatValue() == 0) {
				return true;
			}
		} else if (sourceFieldType.equals(byte.class)) {
			Byte value = (Byte) sourceValue;
			if (value.byteValue() == 0) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 不同类型，值的转换
	 * 
	 * @param sourceValue
	 *            源值
	 * @param sourceFieldType
	 *            源字段类型
	 * @param targetFieldType
	 *            目标字段类型
	 * @return 结果值
	 * @throws ParseException
	 */
	public static Object convertValue(Object sourceValue,
			Class sourceFieldType, Class targetFieldType) throws ParseException {
		if (sourceValue == null) {
			return null;
		}
		if (sourceFieldType.equals(targetFieldType)) {
			return sourceValue;
		}
		// 默认为空（未定类型之间转换）
		Object result = null;
		// 源值为字符串
		if (sourceFieldType.equals(String.class)) {
			String value = (String) sourceValue;
			value = value.trim();
			String intValue = value;
			if (value.indexOf(".") >= 0) {
				intValue = value.substring(0, value.indexOf("."));
			}
			if (targetFieldType.equals(Date.class)) {
				Date dateValue = null;
				if (value.indexOf(" ") != -1) {
					dateValue = DF.parse(value);
				} else {
					if (value.indexOf("-") != -1) {
						dateValue = DF_DATE.parse(value);
					} else if (value.indexOf(":") != -1) {
						dateValue = DF_TIME.parse(value);
					}
				}
				result = dateValue;
			} else if (targetFieldType.equals(Integer.class)) {
				result = Integer.parseInt(intValue);
			} else if (targetFieldType.equals(Long.class)) {
				result = Long.parseLong(intValue);
			} else if (targetFieldType.equals(Double.class)) {
				result = Double.parseDouble(value);
			} else if (targetFieldType.equals(Short.class)) {
				result = Short.parseShort(intValue);
			} else if (targetFieldType.equals(Float.class)) {
				result = Float.parseFloat(value);
			} else if (targetFieldType.equals(Byte.class)) {
				result = Byte.parseByte(intValue);
			} else if (targetFieldType.equals(Character.class)) {
				result = value.charAt(0);
			}
		}
		// 源值为数字
		else if (sourceFieldType.equals(Integer.class)
				|| sourceFieldType.equals(Long.class)
				|| sourceFieldType.equals(Double.class)
				|| sourceFieldType.equals(Short.class)
				|| sourceFieldType.equals(Float.class)
				|| sourceFieldType.equals(Byte.class)) {
			String value = String.valueOf(sourceValue);
			String intValue = value;
			if (value.indexOf(".") >= 0) {
				intValue = value.substring(0, value.indexOf("."));
			}
			if (targetFieldType.equals(String.class)) {
				result = value;
			} else if (targetFieldType.equals(Date.class)) {
				Long longValue = Long.parseLong(intValue);
				Date dateValue = new Date(longValue);
				result = dateValue;
			} else if (targetFieldType.equals(Long.class)) {
				result = Long.parseLong(intValue);
			} else if (targetFieldType.equals(Double.class)) {
				result = Double.parseDouble(value);
			} else if (targetFieldType.equals(Short.class)) {
				result = Short.parseShort(intValue);
			} else if (targetFieldType.equals(Float.class)) {
				result = Float.parseFloat(value);
			} else if (targetFieldType.equals(Byte.class)) {
				result = Byte.parseByte(intValue);
			} else if (targetFieldType.equals(Character.class)) {
				result = value.charAt(0);
			}
		}
		// 源值为日期
		else if (sourceFieldType.equals(Date.class)) {
			Date value = (Date) sourceValue;
			if (targetFieldType.equals(String.class)) {
				result = DF.format(value);
			} else if (targetFieldType.equals(Long.class)) {
				result = value.getTime();
			}
		}
		// 源值为字符
		else if (sourceFieldType.equals(Character.class)) {
			String value = (String) sourceValue;
			value = value.trim();
			if (targetFieldType.equals(Integer.class)) {
				result = Integer.parseInt(value);
			} else if (targetFieldType.equals(Byte.class)) {
				result = Byte.parseByte(value);
			}
		}

		return result;
	}

	/**
	 * 追踪当前类及父类所有字段名
	 * 
	 * @param currentClass
	 *            当前类
	 */
	private void traceAllField(Class currentClass) {
		// 追踪到Object类则终止
		if (currentClass.equals(Object.class)) {
			return;
		}
		// 获取源类对象的所有属性
		Field[] fields = currentClass.getDeclaredFields();
		for (Field field : fields) {
			// 避免属性名重复，跳过属性重写
			if (fieldNameList.indexOf(field.getName()) == -1) {
				fieldNameList.add(field.getName());
			}
		}
		// 获取父类
		Class superClass = currentClass.getSuperclass();
		// 递归调用
		traceAllField(superClass);
	}

	/**
	 * 由字段名追踪对应字段对象
	 * 
	 * @param currentClass
	 *            当前类
	 * @param fieldName
	 *            字段名
	 * @return 字段对象
	 */
	private Field traceFieldByName(Class currentClass, String fieldName) {
		if (currentClass.equals(Object.class)) {
			return null;
		}
		Field result = null;
		try {
			result = currentClass.getDeclaredField(fieldName);
		} catch (SecurityException e) {
			// 安全异常
		} catch (NoSuchFieldException e) {
			Class superClass = currentClass.getSuperclass();
			result = traceFieldByName(superClass, fieldName);
		}

		return result;
	}

	/**
	 * 构造属性存取方法名
	 * 
	 * @param field
	 *            字段名
	 * @param access
	 *            存取方式[get|set]
	 * @return 方法名
	 */
	private String buildMethodName(String field, String access) {
		String firstChar = String.valueOf(field.charAt(0));
		String lastString = field.substring(1, field.length());
		String result = access + firstChar.toUpperCase() + lastString;

		return result;
	}

	public static void main(String[] args) {
		//TODO: Test code

		System.out.println("oK");
	}

}

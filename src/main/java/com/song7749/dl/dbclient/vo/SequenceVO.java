package com.song7749.dl.dbclient.vo;
import com.song7749.dl.base.AbstractVo;


/**
 * <pre>
 * Class Name : SequenceVO.java
 * Description : 시퀀스 조회 객체
*
*  Modification Information
*  Modify Date 		Modifier	Comment
*  -----------------------------------------------
*  2016. 3. 17.		song7749	신규작성
*
* </pre>
*
* @author song7749
* @since 2016. 3. 17.
*/
public class SequenceVO extends AbstractVo {

	private static final long serialVersionUID = -1119797275341824033L;

	private String name;
	private String lastValue;
	private String minValue;
	private String maxValue;
	private String incrementBy;

	public SequenceVO() {}

	public SequenceVO(String name, String lastValue, String minValue,
			String maxValue, String incrementBy) {
		this.name = name;
		this.lastValue = lastValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.incrementBy = incrementBy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastValue() {
		return lastValue;
	}

	public void setLastValue(String lastValue) {
		this.lastValue = lastValue;
	}

	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	public String getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(String maxValue) {
		maxValue = maxValue;
	}

	public String getIncrementBy() {
		return incrementBy;
	}

	public void setIncrementBy(String incrementBy) {
		this.incrementBy = incrementBy;
	}
}
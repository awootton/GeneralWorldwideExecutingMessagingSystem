package org.messageweb.rest;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "AtwTable2")
public class AnotherAtwTableItem {

	String key = "AnotherAtwTableItem_testb;lablabla";
	String value = "none";
	List<String> names = new ArrayList<>();
	double aaValiue = 123.45;
	
	public AnotherAtwTableItem( String key){
		this.key = key;
	}
	
	public AnotherAtwTableItem() {// required for Dynamo 
	}


	@DynamoDBHashKey(attributeName = "id")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}
	
	public void addName(String n){
		names.add(n);
	}

	public double getAaValiue() {
		return aaValiue;
	}

	public void setAaValiue(double aaValiue) {
		this.aaValiue = aaValiue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(aaValiue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnotherAtwTableItem other = (AnotherAtwTableItem) obj;
		if (Double.doubleToLongBits(aaValiue) != Double.doubleToLongBits(other.aaValiue))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (names == null) {
			if (other.names != null)
				return false;
		} else if (!names.equals(other.names))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	

	
}

package models;

import java.util.List;

import com.delhivery.constants.DelhiveryCenterType;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class DelhiveryCenter {
	String name;
	String code; 
	//Address address;
	DelhiveryCenterType dcType;
	List<Area> areasServiceable;
	DCFacilities dcFacilities;
	
	
	public static enum DCFacilities{
		TEMP_CONTROLLED,
		HEAVY
	}
}
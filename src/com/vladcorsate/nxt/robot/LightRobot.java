package com.vladcorsate.nxt.robot;

import lejos.nxt.*;
import lejos.robotics.Color;
import lejos.robotics.navigation.*;

public class LightRobot implements ButtonListener, SensorPortListener {
	
	static int NONE_LEVEL = 30;
	static int LEFT_LEVEL = 26;
	static int FW_LEVEL = 18;
	static int RIGHT_LEVEL = 14;
	static int BW_LEVEL = 10;
	static int STOP_LEVEL = 6;
	static int TOLERANCE = 2;
	
	private ColorSensor cs = null;
	private TouchSensor bump = null;
	DifferentialPilot pilot = null;
	
	public LightRobot (){
		cs = new ColorSensor(SensorPort.S1, Color.NONE);
		pilot = preparePilot();
		bump = new TouchSensor(SensorPort.S4);
	}
	
	public void buttonPressed(Button b){
		if(b == Button.ENTER && NONE_LEVEL > 0){
			int oldLightLevel = 0;
			while(!bump.isPressed()){
//				LCD.clear();
				int lightLevel = cs.getLightValue();
				int delta = Math.abs(lightLevel - oldLightLevel);
				
				if (Math.abs(lightLevel - NONE_LEVEL) <= TOLERANCE || delta <= TOLERANCE ){
					//do nothing
					continue;
				}
				
				String result = doCommand(pilot, lightLevel);
				System.out.println("The robot is " + result);
				
				oldLightLevel = lightLevel;
				
			}
			return;
		}
		if(b == Button.RIGHT){
			cs.calibrateHigh();
			NONE_LEVEL = 0;
			LEFT_LEVEL = 0;
			FW_LEVEL = 0;
			RIGHT_LEVEL = 0; 
			BW_LEVEL = 0;
			STOP_LEVEL = 0;
			LCD.clear();
			System.out.println("Calibrated high");
			return;
		}
		if(b == Button.LEFT){
			cs.calibrateLow();
			NONE_LEVEL = 0;
			LEFT_LEVEL = 0;
			FW_LEVEL = 0;
			RIGHT_LEVEL = 0; 
			BW_LEVEL = 0;
			STOP_LEVEL = 0;
			LCD.clear();
			System.out.println("Calibrated low");
			return;
		}
	}
	
	public void buttonReleased(Button b){
		
	}
	
	public void stateChanged(SensorPort port, int val, int oldVal){
		if(port == SensorPort.S4 && bump.isPressed()){
			//calibrate intermediate levels
			while(bump.isPressed()){
				int lightLevel = cs.getLightValue();
				LCD.clear();
				if(NONE_LEVEL == 0){
					NONE_LEVEL = lightLevel;
					System.out.println("NONE_LEVEL=" + lightLevel + "," + val + "," + oldVal);
					return;
				}
				if(LEFT_LEVEL == 0){
					LEFT_LEVEL = lightLevel;
					System.out.println("LEFT_LEVEL=" + lightLevel + "," + val + "," + oldVal);
					return;
				}
				if(FW_LEVEL == 0){
					FW_LEVEL = lightLevel;
					System.out.println("FW_LEVEL=" + lightLevel + "," + val + "," + oldVal);
					return;
				}
				if(RIGHT_LEVEL == 0){
					RIGHT_LEVEL = lightLevel;
					System.out.println("RIGHT_LEVEL=" + lightLevel + "," + val + "," + oldVal);
					return;
				}
				if(BW_LEVEL == 0){
					BW_LEVEL = lightLevel;
					System.out.println("BW_LEVEL=" + lightLevel + "," + val + "," + oldVal);
					return;
				}
				if(STOP_LEVEL == 0){
					STOP_LEVEL = lightLevel;
					System.out.println("STOP_LEVEL=" + lightLevel + "," + val + "," + oldVal);
					return;
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		LightRobot lightRobot = new LightRobot();
		//add listeners
		Button.ENTER.addButtonListener(lightRobot);
		Button.RIGHT.addButtonListener(lightRobot);
		Button.LEFT.addButtonListener(lightRobot);
		SensorPort.S4.addSensorPortListener(lightRobot);
		
		System.out.println("Press ENTER to start.");
		System.out.println("Press arrows to calibrate");
		System.out.println("Press bumper to calibrate levels");
		
		//ending the program
		Button.ESCAPE.waitForPressAndRelease();

	}
	
	protected static DifferentialPilot preparePilot(){
		DifferentialPilot pilot = new DifferentialPilot(3.8f, 9.5f, Motor.A, Motor.C, true);
		pilot.setRotateSpeed(30);
		
		return pilot;
	}
	
	protected static String doCommand(DifferentialPilot pilot, int lightLevel){
		String result = "doing nothing " + lightLevel;
		if (lightLevel <= STOP_LEVEL){
			//stop the motor
			if (pilot.isMoving())
				pilot.stop();
		}else if(STOP_LEVEL < lightLevel && lightLevel <= BW_LEVEL ){
			//go bw
			if (pilot.isMoving())
				pilot.stop();
			pilot.arc(0, 180);
			pilot.forward();
			result = "moving backwards";
		}else if(BW_LEVEL < lightLevel && lightLevel <= RIGHT_LEVEL){
			//go right
			if (pilot.isMoving())
				pilot.stop();
			pilot.arc(0, -90);
			pilot.forward();
			result  = "turning right";
		}else if(RIGHT_LEVEL <= lightLevel && lightLevel <= FW_LEVEL){
			//go fw
			if (pilot.isStalled())
				pilot.forward();
			result = "moving forward";
		}else if(FW_LEVEL < lightLevel && lightLevel <= LEFT_LEVEL){
			if (pilot.isMoving())
				pilot.stop();
			pilot.arc(0, 90);
			pilot.forward();
			result = "turning left";
		}
		
		return result;
		
	}

}


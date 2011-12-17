package com.vladcorsate.nxt.robot;

import lejos.nxt.*;
import lejos.robotics.Color;
import lejos.robotics.navigation.*;

import java.io.*;

public class LightRobot implements ButtonListener, SensorPortListener {
	
	static int NONE_LEVEL = 0;
	static int LEFT_LEVEL = 0;
	static int FW_LEVEL = 0;
	static int RIGHT_LEVEL = 0;
	static int BW_LEVEL = 0;
	static int STOP_LEVEL = 0;
	static int TOLERANCE = 2;
	
	static boolean calibratedHigh = false;
	static boolean calibratedLow = false;
	
	static final String calibrationFile = "calibrate.dat";
	
	private ColorSensor cs = null;
	private TouchSensor bump = null;
	DifferentialPilot pilot = null;
	private File calFile = null;
	private FileOutputStream fileOut = null;
	private FileInputStream fileIn = null;
	
	public LightRobot (){
		cs = new ColorSensor(SensorPort.S1, Color.NONE);
		pilot = preparePilot();
		bump = new TouchSensor(SensorPort.S4);
		
		calFile = new File(calibrationFile);
	}
	
	private void saveLevels(){
		try{
			fileOut = new FileOutputStream(calFile, false);
			DataOutputStream out = new DataOutputStream(fileOut);
			out.writeInt(NONE_LEVEL);
			out.writeInt(LEFT_LEVEL);
			out.writeInt(FW_LEVEL);
			out.writeInt(RIGHT_LEVEL);
			out.writeInt(BW_LEVEL);
			out.writeInt(STOP_LEVEL);
			out.flush();
			out.close();
		}catch(IOException e){
			System.out.println("Failed to create output stream!");
			Button.waitForPress();
			System.exit(1);
		}
		
	}
	
	private void loadLevels(){
		if (NONE_LEVEL > 0){
			System.out.println("Loaded the levels!");
			return;
		}
		try{
			System.out.println("Reading levels from file!");
			fileIn = new FileInputStream(calFile);
			DataInputStream in = new DataInputStream(fileIn);
			NONE_LEVEL = in.readInt();
			LEFT_LEVEL = in.readInt();
			FW_LEVEL = in.readInt();
			RIGHT_LEVEL = in.readInt();
			BW_LEVEL = in.readInt();
			STOP_LEVEL = in.readInt();
			System.out.println("NONE_LEVEL=" + NONE_LEVEL);
			in.close();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
		
	}
	
	public void buttonPressed(Button b){
		if(b == Button.ENTER){
			loadLevels();	
			int oldLightLevel = 0;
			while(true){
//				LCD.clear();
				try{
					Thread.sleep(300);
				}catch(Exception E){
					
				}
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
		}
		if(b == Button.RIGHT && !calibratedHigh){
			cs.calibrateHigh();
			resetLevels();
			LCD.clear();
			calibratedHigh = true;
			System.out.println("Calibrated high");
			return;
		}
		if(b == Button.LEFT && !calibratedLow){
			cs.calibrateLow();
			resetLevels();
			LCD.clear();
			calibratedLow = true;
			System.out.println("Calibrated low");
			return;
		}
		if(b == Button.RIGHT && calibratedHigh && calibratedLow){
			//calibrate levels
			int lightLevel = cs.getLightValue();
			LCD.clear();
			if(NONE_LEVEL == 0){
				NONE_LEVEL = lightLevel;
				System.out.println("NONE_LEVEL=" + lightLevel);
				return;
			}
			if(LEFT_LEVEL == 0){
				LEFT_LEVEL = lightLevel;
				System.out.println("LEFT_LEVEL=" + lightLevel);
				return;
			}
			if(FW_LEVEL == 0){
				FW_LEVEL = lightLevel;
				System.out.println("FW_LEVEL=" + lightLevel);
				return;
			}
			if(RIGHT_LEVEL == 0){
				RIGHT_LEVEL = lightLevel;
				System.out.println("RIGHT_LEVEL=" + lightLevel);
				return;
			}
			if(BW_LEVEL == 0){
				BW_LEVEL = lightLevel;
				System.out.println("BW_LEVEL=" + lightLevel);
				return;
			}
			if(STOP_LEVEL == 0){
				STOP_LEVEL = lightLevel;
				System.out.println("STOP_LEVEL=" + lightLevel);
				saveLevels();
				return;
			}
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
//		SensorPort.S4.addSensorPortListener(lightRobot);
		
		System.out.println("Press ENTER to start.");
		System.out.println("Press arrows to calibrate");
		System.out.println("Press bumper to calibrate levels");
		
		//ending the program
		Button.ESCAPE.waitForPressAndRelease();

	}
	
	protected static DifferentialPilot preparePilot(){
		DifferentialPilot pilot = new DifferentialPilot(3.8f, 9.5f, Motor.A, Motor.C, true);
		pilot.setRotateSpeed(15);
		
		return pilot;
	}
	
	public static void resetLevels(){
		NONE_LEVEL = 0;
		LEFT_LEVEL = 0;
		FW_LEVEL = 0;
		RIGHT_LEVEL = 0; 
		BW_LEVEL = 0;
		STOP_LEVEL = 0;
		LCD.clear();
	}
	
	protected static String doCommand(DifferentialPilot pilot, int lightLevel){
		String result = "doing nothing " + lightLevel;
		if (Math.abs(lightLevel - STOP_LEVEL) <= TOLERANCE){
			//stop the motor
			if (pilot.isMoving())
				pilot.stop();
			return result = "stopped " + lightLevel;
		}
		if(Math.abs(lightLevel - BW_LEVEL) <= TOLERANCE){
			//go bw
			if (pilot.isMoving())
				pilot.stop();
			pilot.arc(0, 180);
			pilot.forward();
			return result = "moving backwards " + lightLevel;
		}
		if(Math.abs(lightLevel - RIGHT_LEVEL) <= TOLERANCE){
			//go right
			if (pilot.isMoving())
				pilot.stop();
			pilot.arc(0, -90);
			pilot.forward();
			return result  = "turning right " + lightLevel;
		}
		if(Math.abs(lightLevel - FW_LEVEL) <= TOLERANCE){
			//go fw
			if (!pilot.isMoving())
				pilot.forward();
			return result = "moving forward " + lightLevel;
		}
		if(Math.abs(lightLevel - LEFT_LEVEL) <= TOLERANCE){
			if (pilot.isMoving())
				pilot.stop();
			pilot.arc(0, 90);
			pilot.forward();
			result = "turning left " + lightLevel;
		}
		
		return result;
		
	}

	
}

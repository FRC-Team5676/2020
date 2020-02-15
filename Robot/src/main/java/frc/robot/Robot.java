/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

/* default - DO NOT REMOVE */
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/* needed for 2020 robot */
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.AnalogInput;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  /* Default Robot */
  private final String kDefaultAuto = "Default";
  private final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private SendableChooser<String> m_chooser = new SendableChooser<>();

  /* Solenoid */
  private DoubleSolenoid intake_arm = new DoubleSolenoid(2, 0, 1);
  private DoubleSolenoid.Value intake_arm_value = DoubleSolenoid.Value.kReverse;
  private long intake_arm_time;

  private DoubleSolenoid trolley_lift = new DoubleSolenoid(2, 2, 3);
  private DoubleSolenoid.Value trolley_lift_value = DoubleSolenoid.Value.kReverse;
  private long trolley_lift_time;

  private DoubleSolenoid ball_ramp = new DoubleSolenoid(2, 4, 5);
  private DoubleSolenoid.Value ball_ramp_value = DoubleSolenoid.Value.kReverse;
  private long ball_ramp_time;

  /* Drive Train Motors */
  private WPI_VictorSPX left_back_drive = new WPI_VictorSPX(3);
  private WPI_TalonSRX left_front_drive = new WPI_TalonSRX(4);
  private WPI_VictorSPX right_back_drive = new WPI_VictorSPX(5);
  private WPI_TalonSRX right_front_drive = new WPI_TalonSRX(6);
  private DifferentialDrive robot = new DifferentialDrive(left_front_drive, right_front_drive);
  private Faults left_faults = new Faults();
  private Faults right_faults = new Faults();

  /* Intake / Shooting Motors */
  private Spark top_motor = new Spark(0);
  private Spark bottom_motor = new Spark(1);

  /* Trolley Motors */
  private WPI_VictorSPX follow_trolley_motor = new WPI_VictorSPX(7);
  private WPI_TalonSRX main_trolley_motor = new WPI_TalonSRX(8);

  /* Joysticks */
  private Joystick controller_0 = new Joystick(0);
  private Joystick controller_1 = new Joystick(1);

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    /* Chooser Setup */
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    /* factory default all drives */
    right_front_drive.configFactoryDefault();
    right_back_drive.configFactoryDefault();
    left_front_drive.configFactoryDefault();
    left_back_drive.configFactoryDefault();
    follow_trolley_motor.configFactoryDefault();
    main_trolley_motor.configFactoryDefault();

    /* set up followers */
    right_back_drive.follow(right_front_drive);
    left_back_drive.follow(left_front_drive);
    follow_trolley_motor.follow(main_trolley_motor);

    /* flip values so robot moves forwardard when stick-forwardard/LEDs-green */
    right_front_drive.setInverted(true); // !< Update this
    left_front_drive.setInverted(false); // !< Update this

    /*
     * set the invert of the followers to match their respective master controllers
     */
    right_back_drive.setInverted(InvertType.FollowMaster);
    left_back_drive.setInverted(InvertType.FollowMaster);

    /* adjust sensor phase so sensor moves positive when Talon LEDs are green */
    right_front_drive.setSensorPhase(true);
    left_front_drive.setSensorPhase(true);

    /*
     * WPI drivetrain classes defaultly assume left and right are opposite. call
     * this so we can apply + to both sides when moving forwardard. DO NOT CHANGE
     */
    robot.setRightSideInverted(false);

    /* Set Default Solenoid Positions */
    intake_arm.set(intake_arm_value);
    trolley_lift.set(trolley_lift_value);
    ball_ramp.set(ball_ramp_value);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable chooser
   * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
   * remove all of the chooser code and uncomment the getString line to get the
   * auto name from the text box below the Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure below with additional strings. If using the SendableChooser
   * make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
    case kCustomAuto:
      // Put custom auto code here
      break;
    case kDefaultAuto:
    default:
      // Put default auto code here
      break;
    }
  }

  /**
   * This function is called to ininilize operator control.
   */
  @Override
  public void teleopInit() {

  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {

    /* Shoot Low - Button 1 (A) */
    if (controller_0.getRawButton(1) || controller_1.getRawButton(1)) {
      top_motor.set(0);
      bottom_motor.set(1.0);
    }

    /* Shoot High - Button 2 (B) */
    if (controller_0.getRawButton(2) || controller_1.getRawButton(2)) {
      top_motor.set(-1.0);
      bottom_motor.set(1.0);
    }

    /* Intake Balls - Button 3 (X) */
    if (controller_0.getRawButton(3) || controller_1.getRawButton(3)) {
      top_motor.set(0);
      bottom_motor.set(-1.0);
    }

    /* Turn-off Intake / Shoot Motors */
    if (controller_0.getRawButton(1) || controller_1.getRawButton(1) || controller_0.getRawButton(2)
        || controller_1.getRawButton(2) || controller_0.getRawButton(3) || controller_1.getRawButton(3)) {
      top_motor.set(0);
      bottom_motor.set(0);
    }

    /* Extend / Retract Intake Arm - Button 4 (Y) */
    if (controller_0.getRawButton(4) || controller_1.getRawButton(4)) {
      if (intake_arm_value == Value.kForward) {
        intake_arm.set(DoubleSolenoid.Value.kReverse);
      } else {
        intake_arm.set(DoubleSolenoid.Value.kForward);
      }
      intake_arm_time = System.currentTimeMillis();
    } else {
      if (System.currentTimeMillis() - intake_arm_time > 250) {
        intake_arm_value = intake_arm.get();
      }
    }

    /* Trolley Extend, Lift, & Auto Level - Button 5 (Left Button) */
    if (controller_0.getRawButton(5) || controller_1.getRawButton(5)) {
      if (trolley_lift_value == Value.kForward) {
        trolley_lift.set(DoubleSolenoid.Value.kReverse);
      } else {
        trolley_lift.set(DoubleSolenoid.Value.kForward);
      }
      trolley_lift_time = System.currentTimeMillis();
    } else {
      if (System.currentTimeMillis() - trolley_lift_time > 250) {
        trolley_lift_value = trolley_lift.get();
      }
    }
    /* Raise & Lower Ball Area - Button 6 (Right Button) */
    if (controller_0.getRawButton(6) || controller_1.getRawButton(6)) {
      if (ball_ramp_value == DoubleSolenoid.Value.kForward) {
        ball_ramp.set(DoubleSolenoid.Value.kReverse);
      } else {
        ball_ramp.set(DoubleSolenoid.Value.kForward);
      }
      ball_ramp_time = System.currentTimeMillis();
    } else {
      if (System.currentTimeMillis() - ball_ramp_time > 250) {
        ball_ramp_value = ball_ramp.get();
      }
    }
    /* Button 7 (Back Button) */

    /* Button 8 (Start Button) */

    /* Button 9 (Left Joystick Press Down) */

    /* Button 10 (Right Joystick Press Down) */

    /* Drive Robot - Axis 0 & 1 (X & Y Left Joystick) */
    double turn_0 = +1 * controller_0.getRawAxis(0); /* positive is right */
    double turn_1 = +1 * controller_1.getRawAxis(0); /* positive is right */
    double forward_0 = +1 * controller_0.getRawAxis(1); /* positive is forwardard */
    double forward_1 = +1 * controller_1.getRawAxis(1); /* positive is forwardard */

    /* Trolley Up & Down - Axis 2 & 3 (Left & Right Trigger) */
    double trolley_up_down_left_0 = +1 * controller_0.getRawAxis(2);
    double trolley_up_down_right_0 = +1 * controller_0.getRawAxis(3);
    double trolley_up_down_left_1 = +1 * controller_1.getRawAxis(2);
    double trolley_up_down_right_1 = +1 * controller_1.getRawAxis(3);

    /* Reverse Drive Robot - Axis 4 & 5 (X & Y Right Joystick) */
    double turn_0_reverse_drive = -1 * controller_0.getRawAxis(4); /* positive is right */
    double turn_1_reverse_drive = -1 * controller_1.getRawAxis(4); /* positive is right */
    double forward_0_reverse_drive = -1 * controller_0.getRawAxis(5); /* positive is forwardard */
    double forward_1_reverse_drive = -1 * controller_1.getRawAxis(5); /* positive is forwardard */

    /* Controller Deadband 10% */
    if (Math.abs(forward_0) < 0.10) {
      forward_0 = 0;
    }
    if (Math.abs(turn_0) < 0.10) {
      turn_0 = 0;
    }
    if (Math.abs(forward_0_reverse_drive) < 0.10) {
      forward_0_reverse_drive = 0;
    }
    if (Math.abs(turn_0_reverse_drive) < 0.10) {
      turn_0_reverse_drive = 0;
    }
    if (Math.abs(trolley_up_down_right_0) < 0.10) {
      trolley_up_down_right_0 = 0;
    }
    if (Math.abs(trolley_up_down_left_0) < 0.10) {
      trolley_up_down_left_0 = 0;
    }
    if (Math.abs(forward_1) < 0.10) {
      forward_1 = 0;
    }
    if (Math.abs(turn_1) < 0.10) {
      turn_1 = 0;
    }
    if (Math.abs(forward_1_reverse_drive) < 0.10) {
      forward_1_reverse_drive = 0;
    }
    if (Math.abs(turn_1_reverse_drive) < 0.10) {
      turn_1_reverse_drive = 0;
    }
    if (Math.abs(trolley_up_down_right_1) < 0.10) {
      trolley_up_down_right_1 = 0;
    }
    if (Math.abs(trolley_up_down_left_1) < 0.10) {
      trolley_up_down_left_1 = 0;
    }

    /* Trolley Up & Down */
    if (trolley_up_down_left_0 + trolley_up_down_right_0 > 1.5
        || trolley_up_down_left_1 + trolley_up_down_right_1 > 1.5) {
      if (trolley_lift_value == Value.kForward) {
        trolley_lift.set(DoubleSolenoid.Value.kReverse);
      } else {
        trolley_lift.set(DoubleSolenoid.Value.kForward);
      }
      trolley_lift_time = System.currentTimeMillis();
    } else {
      if (System.currentTimeMillis() - trolley_lift_time > 250) {
        trolley_lift_value = trolley_lift.get();
      }
    }

    /* Drive Robot */
    if (forward_0 != 0 || turn_0 != 0) {
      robot.arcadeDrive(forward_0, turn_0);
    }
    if (forward_0_reverse_drive != 0 || turn_0_reverse_drive != 0) {
      robot.arcadeDrive(forward_0_reverse_drive, turn_0_reverse_drive);
    }
    if (forward_1 != 0 || turn_1 != 0) {
      robot.arcadeDrive(forward_1, turn_1);
    }
    if (forward_1_reverse_drive != 0 || turn_1_reverse_drive != 0) {
      robot.arcadeDrive(forward_1_reverse_drive, turn_1_reverse_drive);
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}

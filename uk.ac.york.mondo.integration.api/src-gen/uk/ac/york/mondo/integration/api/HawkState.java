/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package uk.ac.york.mondo.integration.api;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum HawkState implements org.apache.thrift.TEnum {
  RUNNING(0),
  STOPPED(1),
  UPDATING(2);

  private final int value;

  private HawkState(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static HawkState findByValue(int value) { 
    switch (value) {
      case 0:
        return RUNNING;
      case 1:
        return STOPPED;
      case 2:
        return UPDATING;
      default:
        return null;
    }
  }
}

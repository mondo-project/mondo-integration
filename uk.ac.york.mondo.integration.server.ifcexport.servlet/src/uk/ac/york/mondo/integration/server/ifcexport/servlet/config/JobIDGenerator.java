package uk.ac.york.mondo.integration.server.ifcexport.servlet.config;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class JobIDGenerator {

	  private SecureRandom random = new SecureRandom();

	  public String nextSessionId() {
	    return new BigInteger(130, random).toString(32);
	  }

}

package com.tenio.examples.example4.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * This class is used for configuration loader.
 */
public abstract class FileLoaderBase {

  private BufferedReader file;
  private String line = "";
  private boolean flagGoodFile;

  public FileLoaderBase(String filename) {
    line = "";
    flagGoodFile = true;
    try {
      file = new BufferedReader(new FileReader(filename));
    } catch (FileNotFoundException ex) {
      flagGoodFile = false;
    }
  }

  // removes any commenting from a line of text
  public static String removeCommentingFromLine(String line) {
    // search for any comment and remove
    int idx = line.indexOf("//");

    if (idx != -1) {
      // cut out the comment
      return line.substring(0, idx);
    }
    return line;
  }

  private String getParameterValueAsString(String line) {
    // define some delimiters
    final String delims = "[ ;=,]";
    final Pattern pattern = Pattern.compile(delims);
    var s = pattern.split(line);
    if (s.length > 0) {
      return s[s.length - 1];
    }
    return "";
  }

  private String getNextParameter() throws IOException {
    // this will be the string that holds the next parameter
    String line = null;

    line = file.readLine();

    line = removeCommentingFromLine(line);

    // if the line is of zero length, get the next line from
    // the file
    if (line.length() == 0) {
      return getNextParameter();
    }

    line = getParameterValueAsString(line);
    return line;
  }

  private String getNextToken() throws IOException {
    // strip the line of any commenting
    while (line.equals("")) {
      line = file.readLine();
      line = removeCommentingFromLine(line);
    }

    // find beginning of parameter description
    int begIdx = line.length();
    int endIdx = line.length();

    // define some delimiters
    final String delims = "[ ;=,]+";
    var pattern = Pattern.compile(delims);
    var matcher = pattern.matcher(line);

    // find the end of the parameter description
    if (matcher.find()) {
      begIdx = matcher.end();
      if (matcher.find()) {
        endIdx = matcher.start();
      } else {
        endIdx = line.length();
      }
    }

    String s = line.substring(begIdx, endIdx);

    if (endIdx != line.length()) {
      // strip the token from the line
      line = line.substring(endIdx + 1);
    } else {
      line = "";
    }

    return s;
  }

  // helper methods. They convert the next parameter value found into the
  // relevant type
  public double getNextParameterDouble() throws IOException {
    if (flagGoodFile) {
      return Double.valueOf(getNextParameter());
    }
    throw new RuntimeException("bad file");
  }

  public float getNextParameterFloat() throws IOException {
    if (flagGoodFile) {
      return Float.valueOf(getNextParameter());
    }
    throw new RuntimeException("bad file");
  }

  public int getNextParameterInt() throws IOException {
    if (flagGoodFile) {
      return Integer.valueOf(getNextParameter());
    }
    throw new RuntimeException("bad file");
  }

  public boolean getNextParameterBool() throws IOException {
    if (flagGoodFile) {
      return 0 != Integer.valueOf(getNextParameter());
    }
    throw new RuntimeException("bad file");
  }

  public double getNextTokenAsDouble() throws IOException {
    if (flagGoodFile) {
      return Double.valueOf(getNextToken());
    }
    throw new RuntimeException("bad file");
  }

  public float getNextTokenAsFloat() throws IOException {
    if (flagGoodFile) {
      return Float.valueOf(getNextToken());
    }
    throw new RuntimeException("bad file");
  }

  public int getNextTokenAsInt() throws IOException {
    if (flagGoodFile) {
      return Integer.valueOf(getNextToken());
    }
    throw new RuntimeException("bad file");
  }

  public String getNextTokenAsString() throws IOException {
    if (flagGoodFile) {
      return getNextToken();
    }
    throw new RuntimeException("bad file");
  }

  public boolean isEOF() throws IOException {
    if (flagGoodFile) {
      return !file.ready();
    }
    throw new RuntimeException("bad file");
  }

  public boolean isFileIsGood() {
    return flagGoodFile;
  }
}

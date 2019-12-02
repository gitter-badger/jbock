package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;

/**
 * Demonstrates use of jbock to process command-line
 * arguments in a Java application.
 */
public class Main {

  @CLI
  abstract static class Arguments {

    /**
     * Verbosity enabled?
     */
    @Option(mnemonic = 'v', value = "verbose")
    abstract boolean verbose();

    /**
     * File name and path
     */
    @Option(mnemonic = 'f', value = "file")
    abstract String file();
  }

  public static void main(String[] arguments) {
    Arguments args = Main_Arguments_Parser.create().parseOrExit(arguments);
    System.out.println("The file '" + args.file() + "' was provided and verbosity is set to '" + args.verbose() + "'.");
  }
}

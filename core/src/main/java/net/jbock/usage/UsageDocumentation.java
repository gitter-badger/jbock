package net.jbock.usage;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsageDocumentation {

  static final int CONTINUATION_INDENT_USAGE = 8;

  private final PrintStream err;
  private final int terminalWidth;
  private final Map<String, String> messages;
  private final List<Option> options;
  private final List<Parameter> parameters;
  private final Usage usage;
  private final AnsiStyle ansiStyle;
  private final int maxWidthOptions;
  private final int maxWidthParameters;

  UsageDocumentation(
      PrintStream err,
      int terminalWidth,
      Map<String, String> messages,
      List<Option> options,
      List<Parameter> parameters,
      Usage usage,
      AnsiStyle ansiStyle,
      int maxWidthOptions,
      int maxWidthParameters) {
    this.err = err;
    this.terminalWidth = terminalWidth;
    this.messages = messages;
    this.options = options;
    this.parameters = parameters;
    this.usage = usage;
    this.ansiStyle = ansiStyle;
    this.maxWidthOptions = maxWidthOptions;
    this.maxWidthParameters = maxWidthParameters;
  }

  public void printUsageDocumentation() {
    String optionsFormat = "  %1$-" + maxWidthOptions + "s ";
    String paramsFormat = "  %1$-" + maxWidthParameters + "s ";
    String indent_p = String.join("", Collections.nCopies(maxWidthParameters + 4, " "));
    String indent_o = String.join("", Collections.nCopies(maxWidthOptions + 4, " "));

    err.println(ansiStyle.bold("USAGE"));
    String indent_u = String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " "));
    makeLines(indent_u, usage.usage(" ")).forEach(err::println);
    err.println();
    err.println(ansiStyle.bold("PARAMETERS"));
    for (Parameter parameter : parameters) {
      printItemDocumentation(parameter, String.format(paramsFormat, parameter.name()), indent_p);
    }
    err.println();
    err.println(ansiStyle.bold("OPTIONS"));
    for (Option option : options) {
      printItemDocumentation(option, String.format(optionsFormat, option.name()), indent_o);
    }
  }

  private void printItemDocumentation(Item item, String itemName, String indent) {
    String message = item.descriptionKey().isEmpty() ? null : messages.get(item.descriptionKey());
    List<String> tokens = new ArrayList<>();
    tokens.add(itemName);
    tokens.addAll(Optional.ofNullable(message)
        .map(String::trim)
        .map(s -> s.split("\\s+", -1))
        .map(Arrays::asList)
        .orElseGet(() -> item.description().stream()
            .map(s -> s.split("\\s+", -1))
            .flatMap(Arrays::stream)
            .collect(Collectors.toList())));
    makeLines(indent, tokens).forEach(err::println);
  }


  private List<String> makeLines(String indent, List<String> tokens) {
    List<String> result = new ArrayList<>();
    StringBuilder line = new StringBuilder();
    int i = 0;
    while (i < tokens.size()) {
      String token = tokens.get(i);
      boolean fresh = line.length() == 0;
      if (!fresh && token.length() + line.length() + 1 > terminalWidth) {
        result.add(line.toString());
        line.setLength(0);
        continue;
      }
      if (i > 0) {
        line.append(fresh ? indent : " ");
      }
      line.append(token);
      i++;
    }
    if (line.length() > 0) {
      result.add(line.toString());
    }
    return result;
  }
}

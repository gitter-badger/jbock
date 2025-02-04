package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.jbock.examples.fixture.ParserTestFixture.assertArraysEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestArgumentsTest {

  private final RestArgumentsParser parser = new RestArgumentsParser();

  private final ParserTestFixture<RestArguments> f =
      ParserTestFixture.create(parser::parse);

  private final Map<String, String> messages = new HashMap<>();

  private final String[] expected = {
      "A very good program.",
      "",
      "\u001B[1mUSAGE\u001B[m",
      "  rest-arguments [OPTIONS] REST...",
      "",
      "\u001B[1mPARAMETERS\u001B[m",
      "  REST  Hello yes",
      "",
      "\u001B[1mOPTIONS\u001B[m",
      "  --file FILE  This is dog",
      ""};

  @BeforeEach
  void setup() {
    messages.put("the.file", "This\nis\ndog\n");
    messages.put("the.rest", "Hello\n   yes\n");
    messages.put("description.main", "A very good program.");
  }

  @Test
  void testBundleKey() {
    String[] actual = parser.parse("--help").getLeft()
        .map(notSuccess -> f.getUsageDocumentation(notSuccess, messages))
        .orElseThrow();
    assertArraysEquals(actual, expected);
  }

  @Test
  void testBundleKeyFromResourceBundle() {
    ResourceBundle bundle = mock(ResourceBundle.class);
    when(bundle.getKeys()).thenReturn(new Vector<>(messages.keySet()).elements());
    messages.forEach((k, v) -> when(bundle.getString(eq(k))).thenReturn(v));
    String[] actual = parser.parse("--help").getLeft()
        .map(notSuccess -> f.getUsageDocumentation(notSuccess, toMap(bundle)))
        .orElseThrow();
    assertArraysEquals(actual, expected);
  }

  private Map<String, String> toMap(ResourceBundle bundle) {
    return Collections.list(bundle.getKeys()).stream()
        .collect(Collectors.toMap(Function.identity(), bundle::getString));
  }
}

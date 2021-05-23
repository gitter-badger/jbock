package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class GradleArgumentsTest {

  private final ParserTestFixture<GradleArguments> f =
      ParserTestFixture.create(new GradleArguments_Parser());

  @Test
  void errorShortLongConflict() {
    f.assertThat("-m", "hello", "--message=goodbye").failsWithMessage(
        "Option '--message=goodbye' is a repetition");
  }

  @Test
  void errorMissingValue() {
    // there's nothing after -m
    f.assertThat("-m").failsWithMessage("Missing argument after token: -m");
  }

  @Test
  void errorLongShortConflict() {
    f.assertThat("--message=hello", "-m", "goodbye").failsWithMessage(
        "Option '-m' is a repetition");
  }

  @Test
  void errorLongLongConflict() {
    f.assertThat("--message=hello", "--message=goodbye").failsWithMessage(
        "Option '--message=goodbye' is a repetition");
  }

  @Test
  void errorInvalidOption() {
    f.assertThat("-c1").failsWithMessage("Invalid token: -c1");
    f.assertThat("-c-v").failsWithMessage("Invalid token: -c-v");
    f.assertThat("-c-").failsWithMessage("Invalid token: -c-");
    f.assertThat("-c=v").failsWithMessage("Invalid token: -c=v");
    f.assertThat("-c=").failsWithMessage("Invalid token: -c=");
    f.assertThat("-cX=1").failsWithMessage("Invalid token: -cX=1");
    f.assertThat("-cvv").failsWithMessage("Option '-v' is a repetition");
    f.assertThat("-cvx").failsWithMessage("Invalid token: -cvx");
    f.assertThat("-cvm").failsWithMessage("Missing argument after token: -m");
    f.assertThat("--column-count").failsWithMessage("Invalid option: --column-count");
  }

  @Test
  void testDetachedLong() {
    f.assertThat("--message", "hello").succeeds(
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
  }

  @Test
  void testInterestingTokens() {
    f.assertThat("--message=hello", "b-a-b-a", "--", "->", "<=>", "", " ").succeeds(
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "mainToken", Optional.of("b-a-b-a"),
        "otherTokens", asList("->", "<=>", "", " "));
  }

  @Test
  void testPassEmptyString() {
    f.assertThat("-m", "").succeeds(
        "message", Optional.of(""),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("--message=").succeeds(
        "message", Optional.of(""),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("--message", "").succeeds(
        "message", Optional.of(""),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
  }

  @Test
  void testAllForms() {
    Object[] expectation = {
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList()};
    f.assertThat("-mhello").succeeds(expectation);
    f.assertThat("-m", "hello").succeeds(expectation);
    f.assertThat("--message=hello").succeeds(expectation);
    f.assertThat("--message", "hello").succeeds(expectation);
  }

  @Test
  void testRepeatableShortAttached() {
    f.assertThat("-fbar.txt").succeeds(
        "message", Optional.empty(),
        "file", singletonList("bar.txt"),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("-fbar.txt", "--message=hello").succeeds(
        "message", Optional.of("hello"),
        "file", singletonList("bar.txt"),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
    f.assertThat("--message=hello", "-fbar.txt").succeeds(
        "message", Optional.of("hello"),
        "file", singletonList("bar.txt"),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "otherTokens", emptyList());
  }

  @Test
  void testLongSuppressed() {
    // Long option --cmos is suppressed
    f.assertThat("--cmos").failsWithMessage("Invalid option: --cmos");
  }

  @Test
  void testFlag() {
    f.assertThat("-c", "hello", "hello").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", false,
        "mainToken", Optional.of("hello"),
        "otherTokens", singletonList("hello"));
  }

  @Test
  void testPositionalOnly() {
    f.assertThat("hello", "goodbye").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", false,
        "verbose", false,
        "mainToken", Optional.of("hello"),
        "otherTokens", singletonList("goodbye"));
  }

  @Test
  void twoFlags() {
    f.assertThat("-c", "-v").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", true,
        "otherTokens", emptyList());
  }

  @Test
  void testClustering() {
    f.assertThat("-cv").succeeds(
        "message", Optional.empty(),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", true,
        "otherTokens", emptyList());
    f.assertThat("-cvm", "hello").succeeds(
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", true,
        "otherTokens", emptyList());
    f.assertThat("-cvmhello").succeeds(
        "message", Optional.of("hello"),
        "file", emptyList(),
        "dir", Optional.empty(),
        "cmos", true,
        "verbose", true,
        "otherTokens", emptyList());
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "USAGE",
        "  gradle-arguments [OPTION]... [SOME_TOKEN] [moreTokens]...",
        "",
        "PARAMETERS",
        "  SOME_TOKEN  some token",
        "  moreTokens  some more tokens",
        "",
        "OPTIONS",
        "  -m, --message MESSAGE  the message message goes here",
        "  -f, --file INPUT_FILE  the files",
        "  --dir INPUT_DIR        the dir",
        "  -c, --c                cmos flag",
        "  -v, --verbose         ",
        "");
  }
}

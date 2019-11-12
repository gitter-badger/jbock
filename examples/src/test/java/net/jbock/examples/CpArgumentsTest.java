package net.jbock.examples;

import net.jbock.examples.CpArguments.Control;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CpArgumentsTest {

  private ParserTestFixture<CpArguments> f =
      ParserTestFixture.create(CpArguments_Parser.create());

  @Test
  void errorMissingSource() {
    f.assertThat().failsWithUsageMessage("Missing parameter: <SOURCE>");
    f.assertThat("-r").failsWithUsageMessage("Missing parameter: <SOURCE>");
  }

  @Test
  void errorMissingDest() {
    f.assertThat("a").failsWithUsageMessage("Missing parameter: <DEST>");
    f.assertThat("a", "-r").failsWithUsageMessage("Missing parameter: <DEST>");
    f.assertThat("-r", "a").failsWithUsageMessage("Missing parameter: <DEST>");
  }

  @Test
  void minimal() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b",
        "recursive", false,
        "backup", Optional.empty(),
        "suffix", Optional.empty());
    f.assertThat("b", "a").succeeds(
        "source", "b",
        "dest", "a",
        "recursive", false,
        "backup", Optional.empty(),
        "suffix", Optional.empty());
  }

  @Test
  void dashNotIgnored() {
    f.assertThat("-a", "b").failsWithUsageMessage("Invalid option: -a");
  }

  @Test
  void tooMany() {
    f.assertThat("a", "b", "c").failsWithUsageMessage("Invalid option: c");
  }

  @Test
  void tooManyAndFlag() {
    f.assertThat("-r", "a", "b", "c").failsWithUsageMessage("Invalid option: c");
  }

  @Test
  void flagInVariousPositions() {
    Object[] expected = new Object[]{
        "source", "a",
        "dest", "b",
        "recursive", true,
        "backup", Optional.empty(),
        "suffix", Optional.empty()};
    f.assertThat("-r", "a", "b")
        .succeeds(expected);
    f.assertThat("a", "-r", "b")
        .succeeds(expected);
    f.assertThat("a", "b", "-r")
        .succeeds(expected);
  }

  @Test
  void testEnum() {
    assertEquals(Optional.of(Control.NUMBERED), f.parse("a", "b", "--backup=NUMBERED").backup());
    f.assertThat("-r", "a", "b", "--backup", "SIMPLE")
        .succeeds(
            "source", "a",
            "dest", "b",
            "recursive", true,
            "backup", Optional.of(Control.SIMPLE),
            "suffix", Optional.empty());
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  CpArguments",
        "",
        "SYNOPSIS",
        "  CpArguments [OPTIONS...] <SOURCE> <DEST>",
        "",
        "DESCRIPTION",
        "",
        "SOURCE",
        "",
        "DEST",
        "",
        "OPTIONS",
        "  -r",
        "",
        "  --backup <CONTROL>",
        "",
        "  -s <suffix>",
        "    Override the usual backup suffix",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}

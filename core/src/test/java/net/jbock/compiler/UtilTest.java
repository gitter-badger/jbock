package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilTest {

  @Test
  void testSnakeCase() {
    assertEquals("get_money", EnumName.create("getMoney").snake());
    assertEquals("github", EnumName.create("GIThub").snake());
    assertEquals("money_maker", EnumName.create("money_maker").snake());
    assertEquals("robo_cop_9", EnumName.create("roboCop9").snake());
    assertEquals("is_windowscompatible", EnumName.create("isWINDOWScompatible").snake());
    assertEquals("a_required_int", EnumName.create("aRequiredInt").snake());
  }

  @Test
  void testSnakeToCamel() {
    assertEquals("this_is_snake", EnumName.create("thisIsSnake").snake());
    assertEquals("fancy", EnumName.create("fancy").snake());
    assertEquals("f_ancy", EnumName.create("fAncy").snake());
    assertEquals("f_ancy", EnumName.create("f_ancy").snake());
    assertEquals("f_ancy", EnumName.create("f__ancy").snake());
  }

  @Test
  void testBothWays() {
    for (String s : Arrays.asList(
        "getMoney",
        "gitHub",
        "moneyMaker",
        "roboCop9",
        "isWindowsCompatible",
        "aRequiredInt")) {
      String mapped = EnumName.create(EnumName.create(s).snake()).camel();
      assertEquals(s, mapped);
    }
  }
}

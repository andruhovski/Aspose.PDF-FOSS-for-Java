package org.aspose.pdf.engine.script.js;

import org.aspose.pdf.engine.script.js.lexer.Lexer;
import org.aspose.pdf.engine.script.js.lexer.Token;
import org.aspose.pdf.engine.script.js.lexer.TokenType;
import org.aspose.pdf.engine.script.js.parser.JSSyntaxError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** P1 lexer tests: token streams, ASI tracking, regex-vs-divide disambiguation. */
public class LexerTokenTest {

    private static List<Token> lex(String s) {
        return new Lexer(s).tokenize();
    }

    @Test
    void numberForms() {
        assertEquals(255.0, lex("0xff").get(0).number);
        assertEquals(83.0, lex("0123").get(0).number); // legacy octal
        assertEquals(1500.0, lex("1.5e3").get(0).number);
        assertEquals(0.5, lex(".5").get(0).number);
        assertEquals(10.0, lex("1E1").get(0).number);
    }

    @Test
    void stringEscapes() {
        String bs = String.valueOf((char) 92); // a single backslash
        assertEquals("a\nb\tc", lex("'a" + bs + "nb" + bs + "tc'").get(0).value);
        assertEquals("A", lex("'" + bs + "u0041'").get(0).value);   // unicode escape
        assertEquals("I", lex("'" + bs + "x49'").get(0).value);     // hex escape
        assertEquals("line", lex("'li" + bs + "\nne'").get(0).value); // line continuation
        assertEquals(String.valueOf((char) 7), lex("'" + bs + "7'").get(0).value); // octal
    }

    @Test
    void identifiersWithDollarUnderscoreUnicodeEscape() {
        assertEquals(TokenType.IDENT, lex("$foo_bar").get(0).type);
        assertEquals("ABC", lex("\\u0041BC").get(0).value);
    }

    @Test
    void keywordsVersusIdentifiers() {
        assertEquals(TokenType.KEYWORD, lex("function").get(0).type);
        assertEquals(TokenType.KEYWORD, lex("instanceof").get(0).type);
        assertEquals(TokenType.IDENT, lex("fun").get(0).type);
    }

    @Test
    void punctuatorsLongestMatch() {
        assertEquals(">>>=", lex(">>>=").get(0).value);
        assertEquals("===", lex("===").get(0).value);
        List<Token> t = lex("a>>>b");
        assertEquals(">>>", t.get(1).value);
    }

    @Test
    void commentsLineAndBlockAndNewlineTracking() {
        List<Token> t = lex("a // comment\n b /* x \n y */ c");
        assertEquals("a", t.get(0).value);
        assertEquals("b", t.get(1).value);
        assertTrue(t.get(1).newlineBefore, "b is on a new line");
        assertEquals("c", t.get(2).value);
        assertTrue(t.get(2).newlineBefore, "c follows a block comment containing a newline");
    }

    @Test
    void asiNewlineBeforeFlag() {
        List<Token> t = lex("a\nb");
        assertTrue(t.get(1).newlineBefore);
        List<Token> t2 = lex("a b");
        assertFalse(t2.get(1).newlineBefore);
    }

    @Test
    void regexVersusDivide() {
        List<Token> div = lex("a / b");
        assertEquals(TokenType.PUNCT, div.get(1).type);
        assertEquals("/", div.get(1).value);
        List<Token> rx = lex("x = /ab+c/gi");
        assertEquals(TokenType.REGEXP, rx.get(2).type);
        assertEquals("ab+c", rx.get(2).value);
        assertEquals("gi", rx.get(2).regexFlags);
        assertEquals(TokenType.PUNCT, lex("(a) / b").get(3).type);
    }

    @Test
    void regexWithCharClassContainingSlash() {
        List<Token> t = lex("/[/]/");
        assertEquals(TokenType.REGEXP, t.get(0).type);
        assertEquals("[/]", t.get(0).value);
    }

    @Test
    void unterminatedStringThrows() {
        assertThrows(JSSyntaxError.class, () -> lex("'abc"));
    }

    @Test
    void unterminatedBlockCommentThrows() {
        assertThrows(JSSyntaxError.class, () -> lex("/* abc"));
    }
}

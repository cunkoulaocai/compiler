package lexer;

import symbols.Type;

import java.io.IOException;
import java.util.Hashtable;

public class Lexer {
    public static int line = 1;
    char peek = ' ';
    Hashtable words = new Hashtable(); //保留关键字
    public void reserve(Word w) {
        words.put(w.lexeme, w);
    }
    public Lexer() {
        reserve(new Word("if",Tag.IF));
        reserve(new Word("else",Tag.ELSE));
        reserve(new Word("while",Tag.WHILE));
        reserve(new Word("do",Tag.DO));
        reserve(new Word("break",Tag.BREAK));
        reserve(Word.True);
        reserve(Word.False);
        reserve(Type.Int);
        reserve(Type.Char);
        reserve(Type.Bool);
        reserve(Type.Float);

    }

    void readch() throws IOException {
        peek = (char)System.in.read();
    }

    boolean readch(char c) throws IOException {
        readch();
        if (peek!=c) return false;
        peek = ' ';
        return true;
    }

    public Token scan() throws IOException {
        // 掉过空格,制表符,换行
        for (;;readch()) {
            if (peek==' ' || peek=='\t') continue;
            else if (peek=='\n') line = line+1;
            else break;
        }
        switch (peek) {
            case '&':
                if (readch('&')) return Word.and;
                else return new Token('&');
            case '|':
                if (readch('|')) return Word.or;
                else return new Token('|');
            case '=':
                if (readch('=')) return Word.eq;
                else return new Token('=');
            case '!':
                if (readch('!')) return Word.ne;
                else return new Token('!');
            case '<':
                if (readch('=')) return Word.le;
                else return new Token('<');
            case '>':
                if (readch('=')) return Word.ge;
                else return new Token('>');
        }
        if (Character.isDigit(peek)) {
            // 如果是数字
            int v = 0;
            do {
                v = 10 * v + Character.digit(peek, 10);
                readch();
            }while (Character.isDigit(peek));
            // 如果没有小数点
            if (peek!='.') return new Num(v);
            float x = v;
            float d = 10;
            // 有小数点返回一个实数
            for (;;) {
                readch();
                if (!Character.isDigit(peek)) break;
                x = x + Character.digit(peek, 10)/d;
                d = d*10;
            }
            return new Real(x);
        }

        // 这里就是识别关键字和id
        if (Character.isLetter(peek)) {
            StringBuffer b = new StringBuffer();
            do {
                b.append(peek);
                readch();
            }while (Character.isLetterOrDigit(peek));
            String s = b.toString();
            Word w = (Word)words.get(s);

            // 识别有没有已经存过的id了
            // 如果有直接返回
            if (w!=null) return w;

            // 如果没有重新存一个
            // 之后再返回
            w = new Word(s, Tag.ID);
            words.put(s, w);
            return w;
        }
        // 如果都不是直接返回
        Token tok = new Token(peek);
        peek = ' ';
        return tok;
    }
}

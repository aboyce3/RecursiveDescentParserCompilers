import java.io.*;
import java.util.HashMap;

public class RecursiveParser {
    public static void main(String[] args) {
        try {
            System.out.print("# ");
            Parser p = new Parser(new Lex());
            while (true) {
                int result = p.parse();
                System.out.print("# ");
                p = new Parser(new Lex(), p.declarations);
            }
        } catch (ParseError | IOException ex) {
            System.out.println("\nSyntax Error");
        }
    }
}

class ParseError extends Exception {
}

class Lex {
    static final char END = '$';
    static final char LIT = 'L';
    char yytext;
    char token;

    void next() throws ParseError {
        try {
            if (token != END) {
                int c = System.in.read();
                yytext = (char) c;
                if (c == -1) yytext = token = END;
                else if (yytext == '\n') yytext = token = END;
                else if (yytext == '|') token = yytext;
                else if (yytext == '&') token = yytext;
                else if (yytext == '~') token = yytext;
                else if (yytext == '^') token = yytext;
                else if (yytext == '?') token = yytext;
                else if (yytext == '=') token = yytext;
                else if (yytext == '(') token = yytext;
                else if (yytext == ')') token = yytext;
                else if (yytext == '0') token = LIT;
                else if (yytext == '1') token = LIT;
                else if (yytext >= 'a' && yytext <= 'z') token = LIT;
                else if (yytext == ' ' || yytext == '\n') next();
                else {
                    System.out.println("Unexpected character: " + yytext + "\n");
                    next();
                }
            }
        } catch (IOException e) {
            throw new ParseError();
        }
    }

    Lex() throws ParseError {
        next();
    }

    char check(char tok) { // return lex val if matched else null char
        if (tok == token) return yytext;
        else return 0;
    }

    char match(char tok) throws ParseError { // same but consume the token
        char lexval = check(tok);
        if (lexval != 0) next();
        return lexval;
    }
}


class Parser {
    char id;
    HashMap<Character, Integer> declarations;
    Lex lex;

    Parser(Lex l) {
        lex = l;
        id = lex.yytext;
        declarations = new HashMap<>();
        for (int i = 0; i < 26; i++) declarations.put((char) ('a' + i), 0);
        declarations.put('1', 1);
        declarations.put('0', 0);
    }

    Parser(Lex l, HashMap<Character, Integer> map) {
        lex = l;
        declarations = map;
        id = lex.yytext;
    }

    int parse() throws ParseError, IOException {
        return S();
    }

    int S() throws ParseError, IOException {
        int r = Or();
        if (lex.match(Lex.END) != 0) throw new ParseError();
        while (lex.match('?') != 0) {
            System.out.println(declarations.get(id));
            return declarations.get(id);
        }
        while (lex.match('=') != 0) declarations.put(id, Or());
        return r;
    }

    int Or() throws ParseError, IOException {
        int r = And();
        while (lex.match('|') != 0) {
            r = r | And();
        }
        return r;
    }

    int And() throws ParseError, IOException {
        int r = XOr();
        while (lex.match('&') != 0) {
            r = r & XOr();
        }
        return r;
    }

    int XOr() throws ParseError, IOException {
        int r = F();
        while (lex.match('^') != 0) {
            r = r ^ F();
        }
        return r;
    }

    int F() throws ParseError, IOException {
        if (lex.match('(') != 0) {
            int r = Or();
            if (lex.match(')') != 0) return r;
            else throw new ParseError();
        } else {
            char litval = lex.match(Lex.LIT);
            if (litval >= 'a' && litval <= 'z') {
                return declarations.get(litval);
            } else if (litval == '0' || litval == '1') {
                return declarations.get(litval);
            } else if (lex.match('~') != 0) {
                int r = ~Or();
                return r + 2;
            } else throw new ParseError();
        }
    }

}
  
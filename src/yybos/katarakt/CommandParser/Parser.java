package yybos.katarakt.CommandParser;

// /cmd shutdown /s

import java.util.ArrayList;
import java.util.List;

public class Parser {
    public void parseString (String str) {
        List<Token> tokens = new ArrayList<>();

        StringBuilder temp = new StringBuilder();
        Token token = new Token();

        for (char c : str.toCharArray()) {
            switch (c) {
                case '!': {
                    token.tokenType = Token.Type.SLASH;
                    token.content = temp.toString();

                    tokens.add(token);
                    break;
                }

            }
        }
    }
}

class Token {
    Type tokenType;
    String content;

    public enum Type {
        ARGUMENT, // every argument will be treated as a string
        COMMAND,
        SLASH
    }
}

lexer grammar SandyLexer;

@lexer::members {
    private static final int EXTRA = 1;
}

// Whitespace
NEWLINE            : '\r\n' | 'r' | '\n' ;
WS                 : [\t ]+ -> channel(EXTRA) ;

// Keywords
VAR                : 'var' ;

// Literals
INTLIT             : '0'|[1-9][0-9]* ;
DECLIT             : '0'|[1-9][0-9]* '.' [0-9]+ ;

// Operators
PLUS               : '+' ;
MINUS              : '-' ;
ASTERISK           : '*' ;
DIVISION           : '/' ;
ASSIGN             : '=' ;
LPAREN             : '(' ;
RPAREN             : ')' ;

// Identifiers
ID                 : [_]*[a-z][A-Za-z0-9_]* ;

UNMATCHED          : .  -> channel(EXTRA);
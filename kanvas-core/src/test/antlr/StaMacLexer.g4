lexer grammar StaMacLexer;

channels { COMMENT_CH, WHITESPACE_CH }

// Comment
COMMENT            : '//' ~( '\r' | '\n' )* -> channel(COMMENT_CH) ;

// Whitespace
NEWLINE            : ('\r\n' | 'r' | '\n') -> channel(WHITESPACE_CH) ;
WS                 : [\t ]+ -> channel(WHITESPACE_CH) ;

// Keywords : preamble
SM                 : 'statemachine' ;
INPUT              : 'input' ;
VAR                : 'var' ;
EVENT              : 'event' ;

// Keywords : statements and expressions
PRINT              : 'print';
AS                 : 'as';
INT                : 'Int';
DECIMAL            : 'Decimal';
STRING             : 'String';

// Keywords : SM
START              : 'start';
STATE              : 'state';
ON                 : 'on';
ENTRY              : 'entry';
EXIT               : 'exit';

// Identifiers
ID                 : [_]*[a-z][A-Za-z0-9_]* ;

// Literals
INTLIT             : '0'|[1-9][0-9]* ;
DECLIT             : '0'|[1-9][0-9]* '.' [0-9]+ ;
STRINGLIT          : '"' ~["]* '"' ;

// Operators
PLUS               : '+' ;
MINUS              : '-' ;
ASTERISK           : '*' ;
DIVISION           : '/' ;
ASSIGN             : '=' ;
COLON              : ':' ;
LPAREN             : '(' ;
RPAREN             : ')' ;
LBRACKET           : '{' ;
RBRACKET           : '}' ;
ARROW              : '->' ;

UNMATCHED          : . ;

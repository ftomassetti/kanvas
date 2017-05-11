parser grammar StaMacParser;

options { tokenVocab=StaMacLexer; }

stateMachine : preamble (states+=state)+ EOF ;

preamble  : SM name=ID (elements+=preambleElement)* ;

preambleElement : EVENT name=ID                                            # eventDecl
                | INPUT name=ID COLON type                                 # inputDecl
                | VAR name=ID (COLON type)? ASSIGN initialValue=expression # varDecl
                ;

state : (start=START)? STATE name=ID LBRACKET (blocks+=stateBlock)* RBRACKET ;

stateBlock : ON ENTRY LBRACKET (statements+=statement)* RBRACKET # entryBlock
           | ON EXIT LBRACKET (statements+=statement)* RBRACKET  # exitBlock
           | ON eventName=ID ARROW destinationName=ID            # transitionBlock
           ;

statement : assignment       # assignmentStatement
          | print            # printStatement
          | EXIT             # exitStatement ;

print : PRINT LPAREN expression RPAREN ;

assignment : ID ASSIGN expression ;

expression : left=expression operator=(DIVISION|ASTERISK) right=expression # binaryOperation
           | left=expression operator=(PLUS|MINUS) right=expression        # binaryOperation
           | value=expression AS targetType=type                           # typeConversion
           | LPAREN expression RPAREN                                      # parenExpression
           | ID                                                            # valueReference
           | MINUS expression                                              # minusExpression
           | INTLIT                                                        # intLiteral
           | DECLIT                                                        # decimalLiteral
           | STRINGLIT                                                     # stringLiteral ;

type : INT     # integer
     | DECIMAL # decimal
     | STRING  # string;

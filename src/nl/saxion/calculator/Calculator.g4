grammar Calculator;


program : (statement ';')*  ;



statement : (ID EQ  expression)          # AssignmentStatement
    |       PRINT expression             # PrintStatement
    ;


expression : left=expression op=('*' | '/') right=expression              #MulDivExpression
        |    left=expression op=( '+' | '-' ) right=expression            #AddSubExpression
        |    NUM                                                          #ConstExpression
        |    ID                                                           #VarExpression
        ;

PRINT : 'print';
NUM : '0' | [1-9][0-9]*;
EQ : '=';
ID : [a-zA-Z] [a-zA-Z0-9_-]*;
WS : [ \t\n\r]+ -> skip;


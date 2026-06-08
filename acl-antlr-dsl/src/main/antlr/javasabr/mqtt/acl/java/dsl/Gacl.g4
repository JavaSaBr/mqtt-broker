grammar Gacl;

aclConfig
    : directive* EOF
    ;

directive
    : 'allowPublish' '{' ruleBody '}'     #allowPublishDirective
    | 'denyPublish' '{' ruleBody '}'      #denyPublishDirective
    | 'allowSubscribe' '{' ruleBody '}'   #allowSubscribeDirective
    | 'denySubscribe' '{' ruleBody '}'    #denySubscribeDirective
    ;

ruleBody
    : usersSection topicsSection
    ;

usersSection
    : 'users' '{' userCondition* '}'
    ;

userCondition
    : 'anyUser' '(' ')'                                 #anyUserCondition
    | identityType matcherCall                          #shorthandUserCondition
    | identityBlockType '{' matcherCall* '}'            #blockUserCondition
    | 'allOf' '{' userCondition* '}'                    #allOfCondition
    | 'anyOf' '{' userCondition* '}'                    #anyOfCondition
    ;

identityType
    : 'userName'
    | 'clientId'
    | 'ipAddress'
    ;

identityBlockType
    : 'userNames'
    | 'clientIds'
    | 'ipAddresses'
    ;

matcherCall
    : 'startsWith' '(' STRING ')'     #startsWithMatcher
    | 'contains' '(' STRING ')'       #containsMatcher
    | 'eq' '(' STRING ')'             #eqMatcher
    | 'regex' '(' STRING ')'          #regexMatcher
    | 'anyValue' '(' ')'              #anyValueMatcher
    ;

topicsSection
    : 'topics' '{' topicMatcher* '}'
    ;

topicMatcher
    : 'eq' '(' STRING ')'             #eqTopic
    | 'match' '(' STRING ')'          #matchTopic
    | 'dynamic' '(' STRING ')'        #dynamicTopic
    | 'anyTopic' '(' ')'              #anyTopic
    ;

STRING
    : '"' (~["\\\r\n] | '\\' .)* '"'
    | '\'' (~['\\\r\n] | '\\' .)* '\''
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

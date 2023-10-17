%define elem 0
%macro colon 2
    %ifstr %1
        %ifid %2
            %2:
                dq elem
                db %1, 0
        %else
            %error "Error in ID value"
        %endif
    %else   
        %error "Error in string value"
    %endif
    %define elem %2
%endmacro
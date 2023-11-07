.section .bss
char_count:    .skip 256     

.section .rodata
yes_msg:       .string "YES\n"
no_msg:        .string "NO\n"

.section .text
.globl _start
_start:
    xor %edi, %edi              
    mov $256, %ecx              
    lea char_count, %edi        
    xor %al, %al                
    rep stosb                   

read_first_string:
    call getchar                
    cmp $10, %eax               
    je read_second_string       
    cmp $32, %eax               
    je read_first_string        
    movzbl %al, %edx            
    call to_lower               
    incb char_count(%edx)       
    jmp read_first_string       

read_second_string:
    call getchar                
    cmp $10, %eax               
    je check_anagram            
    cmp $32, %eax               
    je read_second_string       
    movzbl %al, %edx            
    call to_lower               
    decb char_count(%edx)       
    jmp read_second_string      

check_anagram:
    xor %edi, %edi              
    mov $256, %ecx              
check_loop:
    movb char_count(%edi), %al  
    test %al, %al               
    jnz print_no                
    inc %edi                    
    loop check_loop             
    jmp print_yes               

print_yes:
    mov $yes_msg, %ecx          
    call puts                   
    jmp end_program             

print_no:
    mov $no_msg, %ecx           
    call puts                   

end_program:
    mov $1, %eax                
    xor %ebx, %ebx              
    int $0x80                   

.type getchar, @function
getchar:
    mov $3, %eax                
    xor %ebx, %ebx              
    lea buffer, %ecx            
    mov $1, %edx                
    int $0x80                   
    cmp $0, %eax                
    jle done_reading            
    movzbl buffer, %eax         
    ret                         
done_reading:
    mov $-1, %eax               
    ret                         

.type puts, @function
puts:
    mov $4, %eax                
    mov $1, %ebx                
    mov %ecx, %ecx              
    mov $4, %edx                
    int $0x80                   
    ret                         

.type to_lower, @function
to_lower:
    cmp $'A', %al               
    jb .done_lower              
    cmp $'Z', %al               
    ja .done_lower              
    add $32, %al                
.done_lower:
    ret                         

.section .bss
buffer:     .skip 1          

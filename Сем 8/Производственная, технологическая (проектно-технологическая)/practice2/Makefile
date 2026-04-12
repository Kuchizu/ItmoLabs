TEX = xelatex
FLAGS = -interaction=nonstopmode
NAME = report

all: $(NAME).pdf

$(NAME).pdf: $(NAME).tex preamble.tex
	$(TEX) $(FLAGS) $(NAME).tex
	$(TEX) $(FLAGS) $(NAME).tex
	$(TEX) $(FLAGS) $(NAME).tex

clean:
	del /q $(NAME).aux $(NAME).log $(NAME).out $(NAME).toc $(NAME).nav $(NAME).snm $(NAME).vrb 2>nul

distclean: clean
	del /q $(NAME).pdf 2>nul

.PHONY: all clean distclean

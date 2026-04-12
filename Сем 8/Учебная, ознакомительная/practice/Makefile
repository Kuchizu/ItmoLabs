TEX = pdflatex
BIB = bibtex
FLAGS = -interaction=nonstopmode
NAME = report

all: $(NAME).pdf

$(NAME).pdf: $(NAME).tex preamble.tex references.bib
	$(TEX) $(FLAGS) $(NAME).tex
	$(BIB) $(NAME)
	$(TEX) $(FLAGS) $(NAME).tex
	$(TEX) $(FLAGS) $(NAME).tex

clean:
	del /q $(NAME).aux $(NAME).log $(NAME).out $(NAME).toc $(NAME).bbl $(NAME).blg $(NAME).nav $(NAME).snm $(NAME).vrb 2>nul

distclean: clean
	del /q $(NAME).pdf 2>nul

.PHONY: all clean distclean

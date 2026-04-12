TARGET = vkr.pdf
SRC    = vkr.tex

PARTS  = preamble.tex \
	 references.bib

ifeq ($(OS),Windows_NT)
CLEAN_CMD = powershell -NoProfile -Command "Remove-Item -Force -ErrorAction SilentlyContinue '$(TARGET)','*.blg','*.bbl','*.aux','*.log','*.toc','*.out','*.nav','*.snm','*.vrb'; exit 0"
else
CLEAN_CMD = rm -f $(TARGET) *.blg *.bbl *.aux *.log *.toc *.out *.nav *.snm *.vrb
endif

all: $(TARGET)

$(TARGET): $(SRC) $(PARTS)
	pdflatex --shell-escape $(SRC)
	bibtex vkr
	pdflatex --shell-escape $(SRC)
	pdflatex --shell-escape $(SRC)

clean:
	$(CLEAN_CMD)

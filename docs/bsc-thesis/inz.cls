\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{inz}

\LoadClass[12pt]{report}

\RequirePackage{url}

\RequirePackage[a4paper]{anysize}
\marginsize{3cm}{2.5cm}{2cm}{2cm}

\RequirePackage{babel}

\RequirePackage{fancyhdr}
\pagestyle{fancy}
\fancyhf{}
\fancyhead[L]{\slshape \nouppercase{\leftmark}}
\fancyhead[R]{\thepage}

\RequirePackage{latexsym}

\RequirePackage{graphicx}

\RequirePackage{float}
\floatstyle{boxed}
\restylefloat{figure}

\def\advisor#1{\gdef\@advisor{#1}} 
\def\advisorpl#1{\gdef\@advisorpl{#1}} 
\def\titlepl#1{\gdef\@titlepl{#1}}

\def\copyr{
  \thispagestyle{empty}

\hspace{1ex}
\vfill
O\'{s}wiadczam, \'{s}wiadoma(y) odpowiedzialno\'{s}ci karnej za po\'{s}wiadczenie nieprawdy, \.{z}e niniejsz\k{a} prac\k{e} dyplomow\k{a} wykona\l{}am(em) osobi\'{s}cie i samodzielnie, i \.{z}e nie korzysta\l{}am(em) ze \,{z}r\'{o}de\l{} innych ni\.{z} wymienione w pracy.
\vspace{5ex}
\begin{flushright}
  \@author
\end{flushright}
}

\def\tpstuff{
\begin{titlepage}

  \begin{center}
    {\sffamily     
      {\Large{\bfseries{\textsc \schoolname }}}

      \rule{12cm}{2mm}

      {\large \dptname \\}
    }

    \vspace*{25mm}

    {\huge \@author}\\                                                       

    \vspace*{1cm}

    {\huge{\bfseries \@title}}
    \vfill
    {\LARGE{\scshape \thesisname}}\\
    \vspace{2cm}
    {\Large{\scshape \advisorname:} \@advisor }\\                       
    \vspace{2cm}
    \vfill

    {\Large \city{} \@date}    
  \end{center}

\end{titlepage}
}

\def\tocstuff{ 
{\setlength{\parskip}{0pt}
  \pagenumbering{roman}
  \setcounter{page}{1}
  
  \tableofcontents
  \listoffigures
  \listoftables
  
  \newpage
  \pagenumbering{arabic}
  \setcounter{page}{1}
  
  \newpage
}
}

\def\endefs{
  \def\schoolname{AGH University of Science and Technology}
  \def\dptname{Faculty of Electrical Engineering, Automatics, Computer Science\\and Biomedical Engineering\\Department of Applied Computer Science}
  \def\thesisname{Bachelor of Science Thesis}
  \def\advisorname{Advisor}
  \def\city{Krak\'{o}w}
}

\def\pldefs{
  \def\schoolname{Akademia G\'{o}rniczo-Hutnicza\\Im.\ Stanis\l{}awa Staszica w Krakowie}
  \def\dptname{Wydzia\l{} Elektrotechniki, Automatyki, Informatyki i In\.{z}ynierii Biomedycznej\\Katedra Informatyki Stosowanej}
  \def\thesisname{Praca Dyplomowa}
  \def\advisorname{Promotor}
  \def\city{Krak\'{o}w}
}

%options

\DeclareOption{polish}{
  \renewcommand{\maketitle}{
    \pldefs
    \tpstuff
    \copyr
    \tocstuff
  }
}
\DeclareOption{american}{
  \renewcommand{\maketitle}{
    \endefs
    \tpstuff
    \copyr

    \pldefs
    \def\@title{\@titlepl}
    \def\@advisor{\@advisorpl}

    \tpstuff
    \tocstuff
  }
}

\DeclareOption{english}{
  \renewcommand{\maketitle}{
    \endefs
    \tpstuff
    \copyr

    \pldefs
    \def\@title{\@titlepl}
    \def\@advisor{\@advisorpl}

    \tpstuff
    \tocstuff
  }
}

\ProcessOptions


% paragraph gap:
\setlength{\parskip}{0.5\baselineskip plus 0.5ex minus 0.2ex}

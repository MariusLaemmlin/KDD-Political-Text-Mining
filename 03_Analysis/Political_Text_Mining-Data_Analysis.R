##################################################################
# FINAL VERSION OF DATA ANALYSIS USING NEWSPAPER AND SOCIAL DATA #
##################################################################
#
# Author: Willi Koepplin
# Date: 2017-07-20
#
# Run the code using STRG+UMSCHALT+ENTER and press ENTER in Command view to proceed after each analysis

rm(list =ls())

############
# PACKAGES #
############
library(devtools)
library(dplyr)
library(ggplot2)
library(grid)
library(gridExtra)
library(ca)
library(FactoMineR)
library(factoextra)
library(gplots)
library(lubridate)
library(readr)
library(lmtest)
library(PerformanceAnalytics)
library(easyGgplot2)
library(imputeTS)
library(astsa)
library(psych )


#############
# FUNCTIONS #
#############

# Setscrews
no.zeros = TRUE # TRUE when not using zero-sentiment values
no.retweet = FALSE # TRUE when not using retweet data
smooth.degree = 0.2 # Degree of smoothness of fitted curve

# Read- In News data from AIFB
read.news <- function(path.news) {
  #Read In
  news.data <- read.csv(file = path.news, 
                        sep = ";",
                        header = TRUE,
                        col.names = c("ID", "date", "publisher", "type", "senti_entity", "senti_score", "senti_relevance", "senti_count", "orientation", "politician"),
                        colClasses = c(NA, NA, NA, NA, NA, NA, NA, NA, NA, NA), stringsAsFactors = TRUE)
  
  # Restrict on relevant entities (politicians)
  news.data <- news.data[ifelse(grepl("leave", news.data$orientation) == TRUE | grepl("remain", news.data$orientation) == TRUE, TRUE, FALSE),] 
  
  # Transform to DateTime Object
  news.data$date <- as.POSIXct(news.data$date,format="%Y-%m-%d %H:%M:%S")
  
  # Delete zero sentiment entities
  if(no.zeros == TRUE){
    
    news.data <- news.data[news.data$senti_score != 0,]
  }
  
  # For Contra Entities SentiScore has to be multiplicated with -1 as e.g. a postive sentiment towards BJ speaks against remaining
  multipl.vector <- ifelse(grepl("leave", news.data$orientation) == TRUE, -1, 1)
  news.data$senti_score <- news.data$senti_score*multipl.vector
  
  return(news.data)
}

# Read-In Social data from AIFB
read.twitter <- function(path.twitter) {
  #Read In
  twitter.data <- read.csv(file = path.twitter, 
                           sep = ";",
                           header = TRUE,
                           col.names = c("ID", "date", "isRetweet", "publisher", "senti_entity", "senti_score", "senti_relevance", "senti_count", "orientation", "politician"), #publisher is type
                           colClasses = c(NA, NA, NA, NA, NA, NA, NA, NA, NA, NA), stringsAsFactors = FALSE)
  
  # Restrict on relevant entities (politicians)
  twitter.data <- twitter.data[ifelse(grepl("leave", twitter.data$orientation) == TRUE | grepl("remain", twitter.data$orientation) == TRUE, TRUE, FALSE),] 
  
  # If necessary, restrict on retweets
  if(no.retweet == TRUE){
    twitter.data <- twitter.data[ifelse(grepl("false", twitter.data$isRetweet) == TRUE, TRUE, FALSE),]
  }
  
  #Transform to DateTime Object and restrict on relevant time
  twitter.data$date <- as.POSIXct(twitter.data$date,format="%Y-%m-%d %H:%M:%S")
  twitter.data <- twitter.data[twitter.data$date >= "2016-06-01",]
  
  # For Contra Entities SentiScore has to be multiplicated with -1 as e.g. a postive sentiment towards BJ speaks against remaining
  multipl.vector <- ifelse(grepl("leave", twitter.data$orientation) == TRUE, -1, 1)
  twitter.data$senti_score <- twitter.data$senti_score*multipl.vector
  
  # Delete zero sentiment entities
  if(no.zeros == TRUE){
    
    twitter.data <- twitter.data[twitter.data$senti_score != 0,]
  }
  
  return(twitter.data)
}

# Read-In FTSE data (minutely closing price)
read.FTSE.mins <- function(path.FTSE.mins) {
  FTSE.mins <- read_delim(path.FTSE.mins, ";", escape_double = FALSE, col_types = cols(Date = col_datetime(format = "%d.%m.%Y %H:%M")),
                          locale = locale(decimal_mark = ","),
                          trim_ws = TRUE)
  FTSE.mins <- FTSE.mins[FTSE.mins$Date >= as.POSIXct("2016-06-01 01:00:35") & FTSE.mins$Date <= as.POSIXct("2016-06-30 12:59:58"),]
  return(FTSE.mins)
}

# Read-In FTSE data (daily closing price)
read.FTSE.days <- function(path.FTSE.days) {
  FTSE.days <- read_csv("C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/FTSE 100/FTSE_100_Stock_Data.csv",
                        col_types = cols(Date = col_date(format = "%m/%d/%Y")))
  FTSE.days <- FTSE.days[FTSE.days$Date > "2016-05-31" & FTSE.days$Date <= "2016-06-30",]
  return(FTSE.days)
}

# Weighted Mean of IDs with weight factor senti_relevance
meanIDs <- function (data){
  
  data$date <- round.POSIXt(data$date, units = c("mins"))
  data$date <- as.POSIXct(data$date)
  
  mean.ID.sentiment.scores <- data %>%
    group_by(ID, date, publisher) %>%
    summarise(Mean = weighted.mean(x = senti_score, w = senti_relevance, na.rm = TRUE))
  
  return(mean.ID.sentiment.scores)
}

# Analyze different newspapers by filtering on them and fitting a curve using loss prediction
analyzeNewspapersSentiment <- function (data, relevant.newspapers){
  
  mean.plots <- list()
  freq.plots <- list()
  
  for (i in 1:length(relevant.newspapers)){
    
    mean.publisher.sentiment.scores <- meanIDs(data) %>%
      filter(grepl(relevant.newspapers[i], publisher)) %>%
      group_by(date) %>%
      summarise(Mean = mean(Mean, na.rm = TRUE))
    
    mean.plots[[i]] <- ggplot(data=mean.publisher.sentiment.scores, aes(x = date, y = Mean)) +
      geom_point(color = "red4", size = 1) + 
      geom_smooth(alpha = 0.2, linetype = 1, color = "red4", fill = "red", level = 0.95, method = "loess", span = smooth.degree) +scale_x_datetime(date_breaks = "3 day") +
      ggtitle(paste("Newspaper Sentiments for ", relevant.newspapers[i])) + 
      xlab("Date") + ylab("Remain/Leave Sentiscore")
    p1 <- mean.plots[[i]]
    
    grid.arrange(p1, ncol = 1)
    
    readline(prompt= "Press [enter] to continue to next newspaper")
  }
  
  return(list(mean.plots))  
}

# Analyze different newspapers by filtering on them and fitting a curve using loss prediction, for Big7 data (see part "ANALYSIS OF RELATION")
analyzeNewspapersSentimentBig7 <- function (data, relevant.newspapers){
  
  mean.plots <- list()
  freq.plots <- list()
  
  for (i in 1:length(relevant.newspapers)){
  
    mean.publisher.sentiment.scores <- meanIDs(data) %>%
      filter(grepl(relevant.newspapers[i], publisher)) %>%
      group_by(date) %>%
      summarise(Mean = mean(Mean, na.rm = TRUE))
    
    observations <- nrow(mean.publisher.sentiment.scores)
    
    mean.plots[[i]] <- ggplot(data=mean.publisher.sentiment.scores, aes(x = date, y = Mean)) +
      geom_point(color = "red4", size = 1) + 
      geom_smooth(alpha = 0.2, linetype = 1, color = "red4", fill = "red", level = 0.95, method = "loess", span = smooth.degree) + scale_x_datetime(date_breaks = "3 day") +
      ggtitle(paste("Newspaper Sentiments for ", relevant.newspapers[i])) + 
      xlab("Date") + ylab("Remain/Leave Sentiscore")
    
    p1 <- mean.plots[[i]]
    
    grid.arrange(p1, ncol = 1)
    
    readline(prompt= "Press [enter] to continue to next newspaper")
  }
  
  return(list(mean.plots))  
}

# Analyze overall sentiment by fitting a curve using loss prediction
analyzeGeneralSentiment <- function (data){
  
  mean.ID.sentiment.scores <- meanIDs(data)
  observations <- nrow(mean.ID.sentiment.scores)
  p2 <- ggplot(data=mean.ID.sentiment.scores, aes(x = date)) + scale_x_datetime(date_breaks = "3 day") +
    geom_histogram(bins = 50, color="white", fill=rgb(0.2,0.7,0.1,0.4)) + ggtitle("Frequencies") + xlab("Date") + ylab("Frequencies")
  
  mean.ID.sentiment.scores <- mean.ID.sentiment.scores %>%
    group_by(date) %>%
    summarise(Mean = mean(x = Mean, na.rm = TRUE))
  
  p1 <- ggplot(data=mean.ID.sentiment.scores, aes(x = date, y = Mean)) +
    #geom_point(color = "grey", size = 0.5) + 
    #geom_line(color = "grey", size = 0.5) + 
    geom_smooth(alpha = 0.2, linetype = 1, color = "red4", fill = "red", level = 0.95, method = "loess", span = smooth.degree) +
    ggtitle(paste("General Sentiment")) + scale_x_datetime(date_breaks = "3 day") +
    xlab("Date") + ylab("Remain/Leave Sentiscore")
  
  
  grid.arrange(p1, p2, ncol = 1)
  
  readline(prompt= "Press [enter] to continue")
  
  return(list(p1, p2)) 
}

# Analyze overall sentiment by fitting a curve using loss prediction with 
# weighting factor of range of newspapers for Big 7 (see part "ANALYSIS OF RELATION")
analyzeGeneralSentimentBig7 <- function (data){
  
  data$date <- round.POSIXt(data$date, units = c("mins"))
  data$date <- as.POSIXct(data$date)
  
  mean.ID.sentiment.scores <- data %>%
    group_by(ID, date, publisher, weight) %>%
    summarise(Mean = weighted.mean(x = senti_score, w = senti_relevance, na.rm = TRUE))
  
  mean.ID.sentiment.scores <- mean.ID.sentiment.scores
  observations <- nrow(mean.ID.sentiment.scores)
  p2 <- ggplot(data=mean.ID.sentiment.scores, aes(x = date)) + scale_x_datetime(date_breaks = "3 day") +
    geom_histogram(bins = 50, color="white", fill=rgb(0.2,0.7,0.1,0.4)) + ggtitle("Frequencies") + xlab("Date") + ylab("Frequencies")
  
  mean.ID.sentiment.scores <- mean.ID.sentiment.scores %>%
    group_by(date) %>%
    summarise(Mean = weighted.mean(x = Mean, w = weight, na.rm = TRUE))
  
  p1 <- ggplot(data=mean.ID.sentiment.scores, aes(x = date, y = Mean)) +
    #geom_point(color = "grey", size = 0.5) + 
    #geom_line(color = "grey", size = 0.5) + 
    geom_smooth(alpha = 0.2, linetype = 1, color = "red4", fill = "red", level = 0.95, method = "loess", span = smooth.degree) +
    ggtitle(paste("General Sentiment" )) + scale_x_datetime(date_breaks = "3 day") +
    xlab("Date") + ylab("Remain/Leave Sentiscore")
  
  
  grid.arrange(p1, p2, ncol = 1)
  
  readline(prompt= "Press [enter] to continue")
  
  return(list(p1, p2)) 
}

# Correspondance analysis
ca.scatter <- function(data, x, y){
  #data <- meanIDs(data)
  data$category <- cut(data$senti_score, c(-1, -0.8, -0.2, 0.2, 0.8,1))
  data <- subset(data, grepl(paste(newspapers, collapse = "|"), publisher))
  data <- with(data, table(factor(data$publisher),data$category))
  print(data)
  readline(prompt= "Press [enter] to continue")
  print(chisq.test(data))
  readline(prompt= "Press [enter] to continue")
  
  numb.dim.cols<-ncol(data)-1
  numb.dim.rows<-nrow(data)-1
  dimensionality <- min(numb.dim.cols, numb.dim.rows)
  res.ca <- ca(data)
  print(res.ca)
  ca.factom <- CA(data, ncp=dimensionality, graph=FALSE)
  print(ca.factom)
  resclust.rows<-HCPC(ca.factom, nb.clust=-1, metric="euclidean", method="ward", order=TRUE, graph.scale="inertia", graph=FALSE, cluster.CA="rows")
  resclust.cols<-HCPC(ca.factom, nb.clust=-1, metric="euclidean", method="ward", order=TRUE, graph.scale="inertia", graph=FALSE, cluster.CA="columns")
  
  # First Analysis
  plot.CA(ca.factom, axes=c(x,y), autoLab = "auto", cex=0.75)
  readline(prompt= "Press [enter] to continue")
  
  # Second Analysis
  plot(res.ca, mass = FALSE, dim=c(x,y), contrib = "none", col=c("black", "red"), map ="rowgreen", arrows = c(FALSE, TRUE)) #for rows
  readline(prompt= "Press [enter] to continue")
  plot(res.ca, mass = FALSE, dim=c(x,y), contrib = "none", col=c("black", "red"), map ="colgreen", arrows = c(TRUE, FALSE)) #for columns
  readline(prompt= "Press [enter] to continue")
  
  # Third Analysis
  plot(resclust.rows, axes=c(x,y), choice="map", draw.tree=FALSE, ind.names=TRUE, new.plot=TRUE)
  readline(prompt= "Press [enter] to continue")
  plot(resclust.cols, axes=c(x,y), choice="map", draw.tree=FALSE, ind.names=TRUE, new.plot=TRUE)
  readline(prompt= "Press [enter] to continue")
  
  # # Fourth Analysis
  plot(resclust.rows, axes=c(x,y), choice="3D.map", draw.tree=TRUE, ind.names=TRUE, new.plot=TRUE)
  readline(prompt= "Press [enter] to continue")
  plot(resclust.cols, axes=c(x,y), choice="3D.map", draw.tree=TRUE, ind.names=TRUE, new.plot=TRUE)
  readline(prompt= "Press [enter] to continue")
  
}

################
# REQUIREMENTS #
################

# Relevant Newspapers for AIFB Data
newspapers <- c(
  "http://www.express.co.uk",
  "http://www.mirror.co.uk",
  "http://www.dailymail.co.uk",
  "http://www.telegraph.co.uk",
  "http://www.theguardian.com",
  "http://www.thesun.co.uk",
  "http://www.thetimes.co.uk",
  "http://www.ft.com"
)

# Relevant Newspapers for Big7 Data
newspapers.2 <- c(
  " express.co.uk",
  " mirror.co.uk",
  " dailymail.co.uk",
  " telegraph.co.uk",
  " theguardian.com",
  " thesun.co.uk",
  " thetimes.co.uk"
)

############
# READ-INs #
############
path.twitter <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/Twitter_Sentiments_v02/sentiments.txt"
path.news <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/News_Sentiments_v02/sentiments.txt"
path.big7 <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/Big7/sentiments.txt"
path.news.extension <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/Extension/sentiments.txt"
path.FTSE.mins <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/FTSE 100/FTSE_100_Stock_Data_Mins.csv"
path.FTSE.days <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/FTSE 100/FTSE_100_Stock_Data.csv"
path.social.general <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/Social General/sentiments.txt"
path.big7.general <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/News General/sentiments-exclude-zeros.txt"
path.stanford.twitter <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/Stanford/stanford-social.txt"
path.stanford.big7 <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/Stanford/stanford-big7.txt"
path.dictionary.twitter <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/Naive Sentiments/naiveSentimentDate.txt"
path.dictionary.big7 <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/Naive Sentiments/alternativeSentisNewsBig7.txt"
path.big7.weights <- "C:/Users/Willi/Dropbox/R projects/KDD Files/Brexit/Big7/big7_weights.csv"

choice <- readline(prompt= "'News', 'Twitter', 'Big7', or 'All': ")

switch(choice,
  News = {
    news.data <- read.news(path.news)
  },
  Twitter = {
    twitter.data <- read.twitter(path.twitter)
  },
  Big7 = {
    big7.data <- read.news(path.big7)
    big7.weights <- read.csv2(path.big7.weights)
  },
  All = {
    news.data <- read.news(path.news)
    twitter.data <- read.twitter(path.twitter)
    big7.data <- read.news(path.big7)
    big7.weights <- read.csv2(path.big7.weights)
    news.extension <- read.news(path.news.extension)
    FTSE.mins <- read.FTSE.mins(path.FTSE.mins)
    FTSE.days <- read.FTSE.days(path.FTSE.days)
    stanford.twitter <- read_delim(path.stanford.twitter, 
                                   ";", escape_double = FALSE, col_names = c("ID","date", "senti_score"),
                                   col_types = cols(date = col_datetime(format = "%Y-%m-%d %H:%M:%S")),
                                   trim_ws = TRUE)
    stanford.big7 <- read_delim(path.stanford.big7, 
                                ";", escape_double = FALSE, col_names = c("ID", "date", "senti_score"),
                                col_types = cols(date = col_datetime(format = "%Y-%m-%d %H:%M:%S")),
                                trim_ws = TRUE)
    dictionary.twitter <- read_delim(path.dictionary.twitter,
                                delim = ";", escape_double = FALSE, col_names = c("ID", "senti_score", "date"),
                                col_types = cols(date = col_datetime(format = "%Y-%m-%d %H:%M:%S")),
                                trim_ws = TRUE)
    dictionary.big7 <- read_delim(path.dictionary.big7,
                             delim = ",", escape_double = FALSE, col_names = c("ID", "date", "senti_score"),
                             col_types = cols(date = col_datetime(format = "%Y-%m-%d %H:%M:%S")),
                             trim_ws = TRUE)
    social.general <- read.csv(file = path.social.general, 
                               sep = ";",
                               header = TRUE,
                               col.names = c("ID", "date", "senti_score"),
                               colClasses = c(NA, NA, NA), stringsAsFactors = FALSE)
    big7.general <- read.csv(file = path.big7.general, 
                             sep = ";",
                             header = TRUE,
                             col.names = c("ID", "date", "senti_score"),
                             colClasses = c(NA, NA, NA), stringsAsFactors = FALSE)
    }
)


##########################
# ANALYSIS OF NEWSPAPERS #
##########################

# Seperate Newspaper Analysis
newspaper.plots <- analyzeNewspapersSentiment(data = news.data, relevant.newspapers = newspapers)
newspaper.plots.big7 <- analyzeNewspapersSentimentBig7(data = big7.data, relevant.newspapers = newspapers.2)

# General Sentiment Analysis
sentiment.plot <- analyzeGeneralSentiment(news.data)
big7.weighted.data <- cbind(big7.data, big7.weights)
sentiment.plot.big7 <- analyzeGeneralSentimentBig7(big7.weighted.data)

# Correspondance Analysis
ca.scatter(data = news.data, x = 1,y = 2)

#######################
# ANALYSIS OF TWITTER #
#######################

# General Sentiment Analysis
sentiment.plot <- analyzeGeneralSentiment(data = twitter.data)

#######################
# ANALYSIS STOCK DATA #
#######################

# AIFB social data
twitter.sentiment <- analyzeGeneralSentiment(data = twitter.data)
twitter.plot <- ggplot() +
  geom_smooth(data=twitter.sentiment[[1]]$data, aes(x = date, y = Mean),alpha = 0.1, linetype = 1, color = "blue4", fill = "blue", level = 0.95, method = "loess", span = smooth.degree) +
  ggtitle("Twitter Sentiment Development") +
  xlab("Date") + ylab("Senti Score")

# Source of Data: http://thebonnotgang.com/tbg/historical-data/
percentages.mins <- vector(mode = "double", length = nrow(FTSE.mins)) + FTSE.mins$Price[1]
percentages.mins <- cbind(FTSE.mins, CumReturn = ((FTSE.mins$Price/percentages.mins)-1))
percentages.mins$CumReturn <- percentages.mins$CumReturn*100
percentages.mins.scaled <- percentages.mins
percentages.mins.scaled$CumReturn <- percentages.mins$CumReturn*5

percentages.days <- vector(mode = "double", length = nrow(FTSE.days)) + FTSE.days$Price[1]
percentages.days <- cbind(FTSE.days, CumReturn = ((FTSE.days$Price/percentages.days)-1))
percentages.days$CumReturn <- percentages.days$CumReturn*100
percentages.days.scaled <- percentages.days
percentages.days.scaled$CumReturn <- percentages.days$CumReturn*5

stock.plot <- ggplot() +
  geom_line(data=percentages.mins, aes(x = Date, y = CumReturn), color = "grey", size = 0.2) +
  geom_line(data=percentages.days, aes(x = as.POSIXct(Date), y = CumReturn), color = "black", size = 1) +
  ggtitle("FTSE Stock Price Cumulative Returns") +
  xlab("Date") + ylab("Cum. Return in %")

grid.arrange(twitter.plot, stock.plot)
readline(prompt = "Press Enter to continue")

### Time series cross correlation minutely

# Time series for tweets minutely
ts.tweets <- meanIDs(twitter.data)[,c("date", "Mean")]
ts.tweets <- ts.tweets %>%
  group_by(date) %>%
  summarise(Mean = mean(x = Mean))
full <- seq(from = as.POSIXct("2016-06-01 01:01:00"), by = "1 min", to = as.POSIXct("2016-06-30 13:00:00"))
ts.tweets <- data.frame(Date = full, Mean = with(ts.tweets, Mean[match(full, date)]))
test <- na.interpolation(ts.tweets)

# Time series for FTSE minutely
ts.FTSE <- FTSE.mins[FTSE.mins$Date<as.POSIXct("2016-06-30 13:00:00"),c("Date", "Price")]
ts.FTSE <- data.frame(Date = full, Price = with(ts.FTSE, Price[match(full, Date)]))
test2 <- na.interpolation(ts.FTSE)

ccfvalues <- ccf(test[,2], test2[,2], lag.max = 10000, main = "Twitter Sentiment and FTSE Development Cross Correlation Function", c("correlation"))
max(ccfvalues[[1]])
readline(prompt = "Press Enter to continue")

#################
# AIFB TO BIG 7 #
#################

# AIFB data
mean.news <- analyzeGeneralSentiment(data = news.data)
mean.twitter <- analyzeGeneralSentiment(data = twitter.data)

# BIG 7
mean.big7 <- analyzeGeneralSentiment(data = big7.data)
big7.weighted.data <- cbind(big7.data, big7.weights)
mean.big7.weighted <- analyzeGeneralSentimentBig7(data = big7.weighted.data)

# Comparison of different sentiment developments, use hashtag to make certain curves invisible
ggplot() +
  geom_smooth(data=mean.twitter[[1]]$data, aes(x = date, y = Mean),alpha = 0.1, linetype = 1, color = "blue4", fill = "blue", level = 0.95, method = "loess", span = smooth.degree) +
  geom_smooth(data=mean.big7.weighted[[1]]$data, aes(x = date, y = Mean),alpha = 0.1, linetype = 1, color = "red4", fill = "red", level = 0.95, method = "loess", span = smooth.degree) +
  geom_smooth(data=mean.big7[[1]]$data, aes(x = date, y = Mean),alpha = 0.1, linetype = 1, color = "yellow4", fill = "yellow", level = 0.95, method = "loess", span = smooth.degree) +
  geom_smooth(data=mean.news[[1]]$data, aes(x = date, y = Mean),alpha = 0.1, linetype = 1, color = "black", fill = "black", level = 0.95, method = "loess", span = smooth.degree) +
  ggtitle(paste("Analysis" )) +
  xlab("Date") + ylab("Sentiscore")


########################
# ANALYSIS OF RELATION #
########################

# Stanford Social Data
stanford.twitter$date <- round.POSIXt(stanford.twitter$date, units = c("mins"))
stanford.twitter$date <- as.POSIXct(stanford.twitter$date)
stanford.twitter.plot2 <- ggplot(data=stanford.twitter, aes(x = date)) +
  geom_histogram(bins = 50, color="white", fill=rgb(0.2,0.7,0.1,0.4)) + scale_x_datetime(date_breaks = "3 day") +
  ggtitle("Frequencies") + xlab("Date") + ylab("Frequencies")

obs <- nrow(stanford.twitter)
stanford.twitter <- stanford.twitter %>%
  filter(date>"2016-06-02 12:00:00") %>%
  group_by(date) %>%
  summarise(Mean = mean(senti_score, na.rm = TRUE))

stanford.twitter.plot <- ggplot() +
  # geom_point(data=stanford.twitter, aes(x = date, y = Mean), color = "orange", size = 0.1, alpha = 0.2) +
  geom_smooth(data=stanford.twitter, aes(x = date, y = Mean), alpha = 0.2, linetype = 1, color = "orange", fill = "orange", level = 0.95, method = "loess", span = smooth.degree) + 
  scale_x_datetime(date_breaks = "3 day") +
  ggtitle(paste("Social General Sentiment - Stanford NLP")) +
  xlab("Date") + ylab("Sentiscore")

grid.arrange(stanford.twitter.plot, stanford.twitter.plot2)
readline(prompt = "Press Enter")

# Stanford Big7 Data
stanford.big7$date <- round.POSIXt(stanford.big7$date, units = c("mins"))
stanford.big7$date <- as.POSIXct(stanford.big7$date)

obs <- nrow(stanford.big7)
stanford.big7 <- stanford.big7 %>%
  filter(date>"2016-06-17 00:00:00") 
stanford.big7.plot2 <- ggplot(data=stanford.big7, aes(x = date)) +
  geom_histogram(bins = 50, color="white", fill=rgb(0.2,0.7,0.1,0.4)) + scale_x_datetime(date_breaks = "3 day") +
  ggtitle("Frequencies") + xlab("Date") + ylab("Frequencies")


stanford.big7.plot <- ggplot() +
  #geom_point(data=stanford.big7, aes(x = date, y = senti_score), color = "orange", size = 0.1, alpha = 1) +
  geom_smooth(data=stanford.big7, aes(x = date, y = senti_score), alpha = 0.2, linetype = 1, color = "orange", fill = "orange", level = 0.95, method = "loess", span = smooth.degree) + 
  scale_x_datetime(date_breaks = "3 day") +
  ggtitle(paste("News General Sentiment - Stanford NLP" )) +
  xlab("Date") + ylab("Sentiscore")

grid.arrange(stanford.big7.plot, stanford.big7.plot2)
readline(prompt = "Press Enter")

# Dictionary Sentiment Twitter
dictionary.twitter$date <- round.POSIXt(dictionary.twitter$date, units = c("mins"))
dictionary.twitter$date <- as.POSIXct(dictionary.twitter$date)

dictionary.twitter.plot2 <- ggplot(data=dictionary.twitter, aes(x = date)) + scale_x_datetime(date_breaks = "3 day") +
  geom_histogram(bins = 50, color="white", fill=rgb(0.2,0.7,0.1,0.4)) + ggtitle("Frequencies") + xlab("Date") + ylab("Frequencies")
obs <- nrow(dictionary.twitter)

dictionary.twitter <- dictionary.twitter %>%
  filter(date>"2016-06-02 12:00:00") %>%
  group_by(date) %>%
  summarise(Mean = mean(senti_score, na.rm = TRUE))

dictionary.twitter.plot <- ggplot() + scale_x_datetime(date_breaks = "3 day") +
  geom_smooth(data=dictionary.twitter, aes(x = date, y = Mean), alpha = 0.2, linetype = 1, color = "green", fill = "green", level = 0.95, method = "loess", span = smooth.degree) +
  #geom_point(data=dictionary.twitter, aes(x = date, y = Mean), color = "green", size = 0.1) +
  ggtitle(paste("Social General Sentiment - Dictionary" )) +
  xlab("Date") + ylab("Sentiscore")

grid.arrange(dictionary.twitter.plot, dictionary.twitter.plot2)
readline(prompt = "Press Enter")

# Dictionary Sentiment BIG7
dictionary.big7$date <- round.POSIXt(dictionary.big7$date, units = c("mins"))
dictionary.big7$date <- as.POSIXct(dictionary.big7$date)

obs <- nrow(dictionary.big7)
dictionary.big7 <- dictionary.big7 %>%
  filter(date>"2016-06-17 00:00:00")

dictionary.big7.plot2 <- ggplot(data=dictionary.big7, aes(x = date)) + scale_x_datetime(date_breaks = "3 day") +
  geom_histogram(bins = 50, color="white", fill=rgb(0.2,0.7,0.1,0.4)) + ggtitle("Frequencies") + xlab("Date") + ylab("Frequencies")

dictionary.big7.plot <- ggplot() + scale_x_datetime(date_breaks = "3 day") +
  geom_smooth(data=dictionary.big7, aes(x = date, y = senti_score), alpha = 0.2, linetype = 1, color = "green", fill = "green", level = 0.95, method = "loess", span = smooth.degree) +
  #geom_point(data=dictionary.big7, aes(x = date, y = Mean), color = "green", size = 0.1) +
  ggtitle(paste("News General Sentiment - Dictionary" )) +
  xlab("Date") + ylab("Sentiscore")

grid.arrange(dictionary.big7.plot, dictionary.big7.plot2)
readline(prompt = "Press Enter")

# Social General
social.general$date <- round.POSIXt(social.general$date, units = c("mins"))
social.general$date <- as.POSIXct(social.general$date)

social.general.plot2 <- ggplot(data=social.general, aes(x = date)) +
  geom_histogram(bins = 50, color="white", fill=rgb(0.2,0.7,0.1,0.4)) + scale_x_datetime(date_breaks = "3 day") +
  ggtitle("Frequencies") + xlab("Date") + ylab("Frequencies")

obs <- nrow(social.general)
social.general <- social.general %>%
  filter(date>"2016-06-02 12:00:00") %>%
  group_by(date) %>%
  summarise(Mean = mean(senti_score, na.rm = TRUE))

social.general.plot <- ggplot() +
  # geom_point(data=social.general, aes(x = date, y = Mean), color = "orange", size = 0.1, alpha = 0.2) +
  geom_smooth(data=social.general, aes(x = date, y = Mean), alpha = 0.2, linetype = 1, color = "blue4", fill = "blue", level = 0.95, method = "loess", span = smooth.degree) + 
  scale_x_datetime(date_breaks = "3 day") +
  ggtitle(paste("Social General Sentiment - IBM NLU" )) + 
  xlab("Date") + ylab("Sentiscore") #+ scale_x_datetime(limits = c(as.POSIXct("2016-06-02"), as.POSIXct("2016-06-30")))

grid.arrange(social.general.plot, social.general.plot2)
readline(prompt = "Press Enter")

# News General
big7.general$date <- round.POSIXt(big7.general$date, units = c("mins"))
big7.general$date <- as.POSIXct(big7.general$date)

big7.general.plot2 <- ggplot(data=big7.general, aes(x = date)) +
  geom_histogram(bins = 50, color="white", fill=rgb(0.2,0.7,0.1,0.4)) + scale_x_datetime(date_breaks = "3 day") +
  ggtitle("Frequencies") + xlab("Date") + ylab("Frequencies")

obs <- nrow(big7.general)
big7.general <- big7.general %>%
  filter(date>"2016-06-17 00:00:00")

big7.general.plot <- ggplot() +
  # geom_point(data=big7.general, aes(x = date, y = senti_score), color = "orange", size = 0.1, alpha = 0.2) +
  geom_smooth(data=big7.general, aes(x = date, y = senti_score), alpha = 0.2, linetype = 1, color = "blue4", fill = "blue", level = 0.95, method = "loess", span = smooth.degree) + 
  scale_x_datetime(date_breaks = "3 day") +
  ggtitle(paste("News General Sentiment - IBM NLU" )) + 
  xlab("Date") + ylab("Sentiscore") #+ scale_x_datetime(limits = c(as.POSIXct("2016-06-02"), as.POSIXct("2016-06-30")))

grid.arrange(big7.general.plot, big7.general.plot2)
readline(prompt = "Press Enter")

# Comparison of different Sentiment Analysis approaches (Stanford, IBM, Dictionary)
grid.arrange(stanford.twitter.plot, social.general.plot, dictionary.twitter.plot)
readLine(prompt: "Enter")
grid.arrange(stanford.big7.plot, big7.general.plot, dictionary.big7.plot)
readLine(prompt: "Enter")

###########################
# EXTREME VALUES ANALYSIS #
###########################

# Extreme Values Analysis, consider Sentiment >|+-0.8| as Extreme Values
extreme.twitter <- twitter.data[twitter.data$senti_score >= 0.8 | twitter.data$senti_score <=-0.8,]
print(nrow(extreme.twitter[twitter.data$senti_score >= 0.8,]))
print(nrow(extreme.twitter[twitter.data$senti_score <= -0.8,]))

extreme.news <- news.data[news.data$senti_score >= 0.8 | news.data$senti_score <=-0.8,]
print(nrow(extreme.news[news.data$senti_score >= 0.8,]))
print(nrow(extreme.news[news.data$senti_score <= -0.8,]))

ggplot() + 
  geom_point(data = extreme.news, aes(x = date, y = senti_score), size = 0.1, color = "red4") + 
  geom_point(data = extreme.twitter, aes(x = date, y = senti_score), size = 0.1, color = "blue4")

ggplot() + 
  geom_density(data = twitter.data, aes(x = senti_score), size = 0.1, fill = "blue", color = "blue4", bins = 100) +
  geom_density(data = news.data, aes(x = senti_score), size = 0.1, fill = "red", color = "red4", bins = 100)  +
  ggtitle(paste("Twitter Sample" )) +
  xlab("Sentiment Score") + ylab("Logarithmic Count")
ggplot() + 
  geom_histogram(data = twitter.data, aes(x = senti_score, y = (..count..)/sum(..count..)), size = 0.1, fill = "blue", color = "blue4", bins = 100, alpha = 0.8) + 
  geom_histogram(data = news.data, aes(x = senti_score, y = (..count..)/sum(..count..)), size = 0.1, fill = "red", color = "red4", bins = 100, alpha = 0.8) +
  ggtitle(paste("Relative Count of Twitter and News Sentiment Distribution" )) +
  xlab("Sentiment Score") + ylab("Relative Count")

e1 <- ggplot() + 
  geom_histogram(data = twitter.data, aes(x = senti_score), size = 0.1, fill = "blue", color = "blue4", bins = 100) + scale_y_log10() + 
  ggtitle(paste("Twitter" )) +
  xlab("Sentiment Score") + ylab("Logarithmic Count")
e2 <- ggplot() + 
  geom_histogram(data = news.data, aes(x = senti_score), size = 0.1, fill = "red", color = "red4", bins = 100) + scale_y_log10() + 
  ggtitle(paste("News" )) +
  xlab("Sentiment Score") + ylab("Logarithmic Count")
grid.arrange(e1, e2)


#################
# MEAN ANALYSIS #
#################

#Newspapers Means
one.dim <- meanIDs(big7.data) %>%
  group_by(publisher) %>%
  summarise(Mean = mean(x=Mean))
one.dim

# Stripchart and Boxplots
ggplot2.stripchart(data=meanIDs(big7.data), xName='publisher',yName='Mean',
                   groupName='publisher', addBoxplot=TRUE, size = 0.1)

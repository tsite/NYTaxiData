function error = test(X,y,theta)

m = length(y);

error = (1/(m))*sum(abs((X*theta - y)));

end
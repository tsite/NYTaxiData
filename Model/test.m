function error = test(X,y,theta)

m = length(y);

error = sum(abs((X*theta - y)))/m;

end
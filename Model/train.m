#Octave script for predicting the time a taxi ride will take
clear;clc;
1;
data = load('-ascii','trip_data_sample.txt');

X = data(:, 2:end); y = data(:, 1);

%for memory purposes
clear('data');

m = length(y); % number of training examples
n = size(X,2);
Xn = X;
for i = 1:n
	Xn = [Xn X.^i];
end

X = Xn;


X = [ones(m, 1), X]; % Add a column of ones to x and the quadratic terms
n = size(X,2);


theta = zeros(n, 1); % initialize fitting parameters

iterations = 400;
alpha = .0001;
lambda = 1;

X_norm = zeros(m,n);
X_norm(:,1) = ones(m,1);

%Normalize
for j = 2:n
	Xvec = X(:,j);
	X_norm(:,j) = (Xvec - ones(m,1)*min(Xvec))/(max(Xvec) - min(Xvec));
end


% run gradient descent
theta = gradientDescent(X_norm, y, theta, lambda, alpha, iterations);



printf('Theta found by gradient descent: \n');

for i=1:n
	printf("\tTheta(%d): %f\n",i-1,theta(i));
end
printf("\n");


testData = load('-ascii','trip_data_test.txt');
Xtest = testData(:,2:end);
m = size(Xtest,1);

n = size(Xtest,2);
Xn = Xtest;

for i = 1:n
	Xn = [Xn Xtest.^i];
end

Xtest = Xn;

Xtest = [ones(m,1), Xtest];
Ytest = testData(:,1);

err = test(Xtest,Ytest,theta);
printf('Average Time Error: %d minutes\n',err/60);

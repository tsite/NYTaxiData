#Octave script for predicting the time a taxi ride will take
clear;clc;
1;
data = load('-ascii','trip_data_sample.txt');

X = data(:, 2:end); y = data(:, 1);

%for memory purposes
clear('data');

m = length(y); % number of training examples
n = size(X,2) + 1; %number of features plus the bias feature

%Scale the features [implementation left to featureScale()]
X_norm = featureScale(X);

 % Add a column of ones (bias column) to x_norm
X_norm = [ones(m,1),X_norm];



% initialize fitting parameters with
%random values in case of symmetry
theta = 10^-4*unifrnd(-1,1,n,1)

iterations = 400;
alpha = .5515;
lambda = 0;


% run gradient descent
theta = gradientDescent(X_norm, y, theta, lambda, alpha, iterations);



printf('Theta found by gradient descent: \n');

for i=1:n
	printf("\tTheta(%d): %f\n",i-1,theta(i));
end
printf("\n");


%VALIDATE MODEL HERE
%USING VALIDATION DATA SEPARATE FROM THE TRAINING DATA

validData = load('-ascii','trip_data_valid.txt');
Xval = validData(:,2:end);
yval = validData(:,1);
clear('validData');

mval = size(Xval,1);

Xval_norm = featureScale(Xval);
Xval_norm = [ones(mval,1) Xval_norm];

[errTrain,errVal] = learnCurve(X_norm,y,Xval_norm,yval,alpha,lambda);

figure();
hold on;
plot(1:size(errTrain,1),errTrain);
plot(1:size(errVal,1),errVal);
hold off;

%TEST THE MODEL HERE
%USING TESTING DATA SEPARATE FROM THE TRAINING AND
%VALIDATION DATA

testData = load('-ascii','trip_data_test.txt');
Xtest = testData(:,2:end);

mtest = size(Xtest,1);

Xtest_norm = featureScale(Xtest);
Xtest_norm = [ones(mtest,1) Xtest_norm];

Ytest = testData(:,1);

err = test(Xtest_norm,Ytest,theta);

printf('Average Time Error: %.2f minutes\n',err/60);

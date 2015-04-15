function theta = gradientDescent(X, y, theta, lambda, alpha, num_iters)

%m = num of training examples
m = length(y); % number of training examples
grad = zeros(length(theta),1); %holds gradient

for iter = 1:num_iters
    printf("\rGradient Descent Running\tIteration: %d",iter);
    fflush(stdout);
    


    %Regularized cost function for Gradient Descent
    [cost,grad] = costFunction(X,y,theta,lambda);
    


    theta = theta - alpha*grad;



end
    printf("\n");

end 
function theta = gradientDescent(X, y, theta, lambda, alpha, num_iters)

%m = num of training examples
m = length(y); % number of training examples
grad = zeros(length(theta),1); %holds gradient

for iter = 1:num_iters
    printf("\rGradient Descent Running\tIteration: %d",iter);
    fflush(stdout);
    
    %Regularized Gradient Descent

    

    grad(1) = 1/m * sum((X*theta - y).*X(:,1));

    for j = 2:size(theta)
        grad(j) = 1/m * sum((X*theta - y).*X(:,j)) + lambda/m * theta(j);
    end


    tmptheta = theta;
    for j = 1:length(theta)
        tmptheta(j) = theta(j) - (alpha*(1/m)*grad(j));
    end

    theta = tmptheta;





    % ============================================================

end
    printf("\n");

end

function [cost,grad] = costFunction(X,y,theta,lambda)

	m = size(X,1);
	grad = zeros(length(theta),1);
	regTheta = theta(2:end);

	cost = (1/(2*m))*sum((X*theta - y).^2) + lambda/(2*m) * sum(regTheta.^2);


	grad(1) = 1/m * sum((X*theta - y).*X(:,1));

	for j = 2:size(theta)
	    grad(j) = 1/m * sum((X*theta - y).*X(:,j)) + lambda/m * theta(j);
	end

end
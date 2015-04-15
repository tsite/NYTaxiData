function [theta,cost_history] = conjGradDescent(X,y,theta,lambda,alpha,num_iters)

%Number of examples
m = length(y);
%Number of features including bias
%necessary for restarting conj. grad.
n = size(X,2);
cost_history = zeros(num_iters,1);

%Holds the Previous gradient
grad_old = zeros(n,1);

%Holds the current gradient
grad = zeros(n,1);

%Holds the previous direction we went
p_old = zeros(n,1);

%Holds the current direction we want
p = zeros(n,1);

lastCut = true;

for iter = 1:num_iters
    printf('\rConjugate Gradient Descent Running\tIteration: %d',iter);
    fflush(stdout);
    

    %Regularized cost function for Gradient Descent
    [cost,grad] = costFunction(X,y,theta,lambda);
    cost_history(iter) = cost;

    
    %determine if we're performing a steepest descent step
    %or a conjugate gradient step
    %NOTE: It's iter-1 because iter starts from 1 not 0

    %restart = mod(iter-1,n) == 0; %|| wasNan == 1;
    if iter == 1
        restart = true;
    else
        restart = false;
    end

    gnorm = norm(grad);
    gnorm_old = norm(grad_old);

    if restart
    	p = -1*grad;
    else
    	p = -1*grad + ((grad'*grad)/(grad_old'*grad_old))*p_old;
    end

    
    %if (iter > 1 && (gnorm > gnorm_old))
    %   alpha = alpha / 2;
    %    if alpha < 0.0001
    %        alpha = 0.0001;
    %    end
    %    lastCut = iter;
    %elseif (iter-lastCut >= 10)
    %    alpha = alpha*2;
    %    lastCut = iter;
    %end

    %if gnorm < 10 && lastCut
    %    alpha = alpha / 2;
    %    lastCut = false;
    %end

    %cost
    %gnorm
    %alpha
    %lastCut

    fflush(stdout);

        

    %Perform the descent update:
    %X(k+1) = X(k) + a(k)*p(k);

    theta = theta + alpha*p;


    %update p_old and grad_old
    p_old = p;
    grad_old = grad;


end
    printf('\n');

end